package se.wingnut.eqt.pipeline.fn;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import se.wingnut.eqt.domain.EnrichedPortfolioCompany;
import se.wingnut.eqt.domain.FundData;
import se.wingnut.eqt.domain.Organization;
import se.wingnut.eqt.domain.PortfolioCompany;
import se.wingnut.eqt.domain.pc.PortfolioCompanyData;
import se.wingnut.eqt.integration.EqtService;

import java.util.List;

public class EnrichPortfolioCompaniesFn extends DoFn<KV<String, KV<PortfolioCompany, Organization>>, EnrichedPortfolioCompany> {
    // This client can likely be reused, rather than created as here in the demo code.
    // Think Inversion of Control aka Dependecy Injection...
    EqtService eqtService = new EqtService();

    // Primarily for testing/mocking
    void setEqtService(EqtService eqtService) {
        this.eqtService = eqtService;
    }

    @ProcessElement
    public void processElement(ProcessContext ctx) {
        KV<String, KV<PortfolioCompany, Organization>> pair = ctx.element();
        PortfolioCompany pc = pair.getValue().getKey();
        Organization o = pair.getValue().getValue();

        // TODO, there's likely a benefit from caching these calls as there's a 1:N relationship between fund and org

        // NOTE: If this was Java 21, we could rely on virtual threads here and just spawn new threads for the I/O this causes.
        // However, in Java 17 they're not available and since the DirectRunner is already parallelizing the work, I've skipped the fork/join here.

        // Enrich via separate call to the organization details REST/scrape endpoint
        PortfolioCompanyData portfolioCompanyData = eqtService.getPortfolioCompanyDetails(pc);

        // Enrich via separate calls to the fund REST/scrape endpoints
        List<FundData> fundData = eqtService.getFundDetails(pc);

        EnrichedPortfolioCompany epc = new EnrichedPortfolioCompany(
                pc.isDivestment(),
                pc._id(),
                pc.country(),
                pc.entryDate(),
                pc.exitDate(),
                pc.path(),
                pc.promotedSdg(),
                pc.sdg(),
                pc.sector(),
                pc.title(),
                pc.topic(),
                // Enriching with the organization data, leave out any duplicate fields (already in the portfolio company JSON) by setting them to null
                new Organization(
                        o.uuid(),
                        o.name(), // Same as pc.title, so we _could_ set this to null to avoid duplicating data. However, it is just one field, so I'll keep it for easier debugging
                        o.homepage_url(),
                        o.country_code(),
                        o.city(),
                        o.founded_on(),
                        o.short_description(),
                        o.employee_count(),
                        o.num_funding_rounds(),
                        o.last_funding_on(),
                        o.total_funding_usd()
                ),
                // Enriching with the organization data from the details page
                portfolioCompanyData,
                // Enriching with the fund data from the fund details page
                fundData
        );

        ctx.output(epc);
    }

}
