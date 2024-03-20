package se.wingnut.eqt.pipeline;

import com.google.gson.Gson;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.SerializableCoder;
import org.apache.beam.sdk.extensions.joinlibrary.Join;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.*;
import se.wingnut.eqt.domain.EnrichedPortfolioCompany;
import se.wingnut.eqt.domain.Organization;
import se.wingnut.eqt.domain.PortfolioCompany;
import se.wingnut.eqt.pipeline.fn.*;

import java.util.List;

public class EnrichPortfolioCompaniesPipelineFactory {
    // Holds the path to navigate down in the api call on the EQT website for fetching portfolio companies
    private static final String PATH = "$.result.data.allSanityCompanyPage.nodes";

    private static final Organization DEFAULT_ORGANIZATION = new Organization(null, null, null, null, null, null, null, null, null, null, null);

    /**
     * @param cfg The configuration to use for filenames etc
     * @return The pipeline ready for running
     */
    public Pipeline createPipeline(PipelineCfg cfg) {
        Pipeline pipeline = Pipeline.create();

        PCollection<PortfolioCompany> portfolioCompaniesFromWeb = pipeline
                .apply("Read JSON containing portfolio companies",
                        TextIO.read().from(cfg.portfolioFromWeb().url())
                                .withCompression(cfg.portfolioFromWeb().compression()))
                .apply("Extract with JsonPath",
                        ParDo.of(new ExtractPortfolioCompanyElementsFromPathDoFn(PATH)))
                .apply("Add discriminator column value (portfoliocompany or divestment)",
                        ParDo.of(new AddDiscriminatorValueFn("false")));

        PCollection<PortfolioCompany> divestmentsFromWeb = pipeline
                .apply("Read JSON containing divestments",
                        TextIO.read().from(cfg.divestmentsFromWeb().url())
                                .withCompression(cfg.divestmentsFromWeb().compression()))
                .apply("Extract with JsonPath",
                        ParDo.of(new ExtractPortfolioCompanyElementsFromPathDoFn(PATH)))
                .apply("Add discriminator column value (portfoliocompany or divestment)",
                        ParDo.of(new AddDiscriminatorValueFn("true")));

        PCollection<PortfolioCompany> allPortfolioCompaniesFromWeb = PCollectionList.of(portfolioCompaniesFromWeb).and(divestmentsFromWeb)
                .apply(Flatten.pCollections());

        // Filter orgs and keep only the orgs in the portfolio (the orgs data is too big to fit in memory for a normal PC)
        PCollection<String> titles = allPortfolioCompaniesFromWeb.apply("Select Title",
                ParDo.of(new SelectTitleFn()));
        // Create a PCollectionView from filterIds
        PCollectionView<List<String>> titleFilterView = titles.apply(View.asList());

        PCollection<String> filteredAdditionalOrganizationDataFromGCP = pipeline
                .apply("Read additional Organization data as downloaded from GCP bucket",
                        TextIO.read().from(cfg.enrichmentOrgsFromGCP().url())
                                .withCompression(cfg.enrichmentOrgsFromGCP().compression()))
                .apply("Filter by title",
                        ParDo.of(new FilterBySideInputFn()).withSideInput("titleFilterView", titleFilterView));

        PCollection<Organization> additionalOrganizationDataFromGCP = filteredAdditionalOrganizationDataFromGCP
                .apply("Parse organizations from JSON strings",
                        ParDo.of(new ParseJsonFn<>(Organization.class))).setCoder(SerializableCoder.of(Organization.class));

        // TODO Join with the fundings reference file: cfg.enrichmentFundsFromGCP().url() to get fundings for the org.
        // In this case it works fine to join on organization uuid, so the equivalent of an SQL:
        // FROM ORGANIZATION o LEFT OUTER JOIN FUNDING f
        // ON o.uuid = f.org_uuid

        PCollection<KV<String, PortfolioCompany>> keyedPortfolioCompanies = allPortfolioCompaniesFromWeb
                .apply("Key PortfolioCompany by join column: title", ParDo.of(new LowerCaseKeyFn<>()));

        PCollection<KV<String, Organization>> keyedOrganizations = additionalOrganizationDataFromGCP
                .apply("Key Organization by join column: name", ParDo.of(new LowerCaseKeyFn<>()));

        // leftOuterJoin since we want to keep all portfolio companies even if they have no additional organization data to enrich with
        PCollection<KV<String, KV<PortfolioCompany, Organization>>> portfolioCompanyOrganizationPairs = Join.leftOuterJoin(keyedPortfolioCompanies, keyedOrganizations, DEFAULT_ORGANIZATION);

        // This is the real enrichment part of the pipeline, everything so far has been building up the data to make this happen
        PCollection<EnrichedPortfolioCompany> enrichedPortfolioCompanies = portfolioCompanyOrganizationPairs
                .apply("Create the enriched domain object from the parts",
                        ParDo.of(new EnrichPortfolioCompaniesFn()));

        enrichedPortfolioCompanies.apply("Serialize records back into JSON",
                        MapElements.into(TypeDescriptors.strings())
                                .via(enrichedPortfolioCompany -> new Gson().toJson(enrichedPortfolioCompany)))
                .apply("Write JSON to resulting file and compress",
                        TextIO.write().to(cfg.finalEnrichedPortfolioCompaniesFile().url())
                                .withCompression(cfg.finalEnrichedPortfolioCompaniesFile().compression())
                                .withoutSharding()); // Produce one output file only here in local env

        return pipeline;
    }

}
