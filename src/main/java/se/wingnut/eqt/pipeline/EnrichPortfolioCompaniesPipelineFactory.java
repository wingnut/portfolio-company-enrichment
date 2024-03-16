package se.wingnut.eqt.pipeline;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.SerializableCoder;
import org.apache.beam.sdk.extensions.joinlibrary.Join;
import org.apache.beam.sdk.io.Compression;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;
import se.wingnut.eqt.domain.Organization;
import se.wingnut.eqt.domain.PortfolioCompany;

public class EnrichPortfolioCompaniesPipelineFactory {
    private static final String PATH = "$.result.data.allSanityCompanyPage.nodes";

    private static final Organization DEFAULT_ORGANIZATION = new Organization(null, null, null, null, null, null, null, null, null, null, null);

    /**
     * Convenience method
     * @return Default pipeline, using default values for names/compression
     */
    public Pipeline createPipeline() {
        return createPipeline("tmp/portfolio-companies-from-web.json", Compression.UNCOMPRESSED, "tmp/organizations-from-gcp.json", Compression.GZIP);
    }

    /**
     * Variation for using test files that may be shorter and/or compressed/uncomressed
     * @param portfolioCompaniesFileName The path to the test file containing portfolio companies (as they would have looked after scraping/downloading from the web)
     * @param portfolioCompaniesCompression Which compression to expect the portfolio companies file in (or uncompressed)
     * @param organizationsFileName The path to the test file containing additional organization data (as they would have looked after downloading from GCP bucket)
     * @param organizationsCompression Which compression to expect the organizations file in (or uncompressed)
     * @return The pipeline ready for running
     */
    public Pipeline createPipeline(String portfolioCompaniesFileName, Compression portfolioCompaniesCompression, String organizationsFileName, Compression organizationsCompression) {
        Pipeline pipeline = Pipeline.create();

        PCollection<String> jsonString = pipeline.apply("Read JSON containing portfolio companies",
                TextIO.read().from(portfolioCompaniesFileName)
                        .withCompression(portfolioCompaniesCompression));

        PCollection<PortfolioCompany> portfolioCompaniesFromWeb = jsonString
                .apply("Extract with JsonPath", ParDo.of(new ExtractPortfolioCompanyElementsFromPathDoFn(PATH)));

        PCollection<Organization> additionalOrganizationDataFromGCP = pipeline
                .apply("Read additional Organization data as downloaded from GCP bucket",
                        TextIO.read().from(organizationsFileName)
                                .withCompression(organizationsCompression))
                .apply("Parse organizations from JSON strings",
                        ParDo.of(new ParseJsonFn<>(Organization.class))).setCoder(SerializableCoder.of(Organization.class));

        // Key portfolio companies the join column
        PCollection<KV<String, PortfolioCompany>> keyedPortfolioCompanies = portfolioCompaniesFromWeb
                .apply("Key PortfolioCompany by join column: title", ParDo.of(new KeyFn<>()));

        PCollection<KV<String, Organization>> keyedOrganizations = additionalOrganizationDataFromGCP
                .apply("Key Organization by join column: name", ParDo.of(new KeyFn<>()));

        // leftOuterJoin since we want to keep all portfolio companies even if they have no additional organization data to enrich with
        PCollection<KV<String, KV<PortfolioCompany, Organization>>> enrichedPortfolioCompanies = Join.leftOuterJoin(keyedPortfolioCompanies, keyedOrganizations, DEFAULT_ORGANIZATION);

        enrichedPortfolioCompanies.apply("Serialize records back into JSON",
                        MapElements.into(TypeDescriptors.strings())
                                .via((SerializableFunction<KV<String, KV<PortfolioCompany, Organization>>, String>) new PortfolioCompanyToJsonFn()))
                .apply("Write JSON to resulting file and compress",
                        TextIO.write().to("src/test/resources/output/enrichedPortfolioCompanies.json")
                                .withoutSharding()); // Produce one output file only here in local env

        return pipeline;
    }
}
