package se.wingnut.eqt.pipeline.fn;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import se.wingnut.eqt.domain.EnrichedPortfolioCompany;
import se.wingnut.eqt.domain.FundData;
import se.wingnut.eqt.domain.Organization;
import se.wingnut.eqt.domain.PortfolioCompany;
import se.wingnut.eqt.domain.pc.PortfolioCompanyData;
import se.wingnut.eqt.domain.pc.PortfolioCompanyDataRaw;
import se.wingnut.eqt.http.SimpleRestClient;
import se.wingnut.eqt.http.fund.Data;
import se.wingnut.eqt.http.fund.FundResponse;
import se.wingnut.eqt.http.fund.Result;
import se.wingnut.eqt.http.pc.PortfolioCompanyResponse;

import java.util.List;
import java.util.stream.Collectors;

public class EnrichPortfolioCompaniesFn extends DoFn<KV<String, KV<PortfolioCompany, Organization>>, EnrichedPortfolioCompany> {
    public final static String FUND_DETAILS_REST_ENDPOINT = "https://eqtgroup.com/page-data%spage-data.json";
    // These just happen to be the same, could be any url
    public final static String ORG_DETAILS_REST_ENDPOINT = "https://eqtgroup.com/page-data%spage-data.json";

    // This client can likely be reused, rather than created as here in the demo code.
    // Think Inversion of Control aka Dependecy Injection...
    SimpleRestClient restClient = new SimpleRestClient();

    // Primarily for testing/mocking
    void setRestClient(SimpleRestClient restClient) {
        this.restClient = restClient;
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
        PortfolioCompanyData portfolioCompanyData = getPortfolioCompanyData(pc);

        // Enrich via separate calls to the fund REST/scrape endpoints
        List<FundData> fundData = getFundDetails(pc);

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

    private PortfolioCompanyData getPortfolioCompanyData(PortfolioCompany pc) {
        String path = pc.path();
        if (path != null && !path.isEmpty()) {
            PortfolioCompanyResponse pcr = restClient.get(ORG_DETAILS_REST_ENDPOINT.formatted(pc.path()), PortfolioCompanyResponse.class);
            PortfolioCompanyDataRaw pcRaw = pcr.result().data().sanityCompanyPage();
            String detailedDescription = null;
            if (pcRaw._rawBody() != null) {
                List<String> childTexts = pcRaw._rawBody().stream()
                        .map(re -> re.children()) // We're only interested in the text field down in the child elements
                        .flatMap(List::stream)
                        .filter(child -> child._type().equals("span")) // We're only interested child elements of type span (that typically contains text)
                        .map(child -> child.text().replaceAll("\n", " ")) // Remove any line breaks
                        .collect(Collectors.toList());
                detailedDescription = String.join(" ", childTexts);
            }
            return new PortfolioCompanyData(pcRaw.slug(), pcRaw.website(), pcRaw.board(), detailedDescription);
        } else {
            return null;
        }
    }

    private List<FundData> getFundDetails(PortfolioCompany pc) {
        return pc.fund().stream()
                .map(fund -> {
                    String path = fund.path();
                    if (path != null && !path.isEmpty()) {
                        return restClient.get(FUND_DETAILS_REST_ENDPOINT.formatted(fund.path()), FundResponse.class);
                    } else {
                        return new FundResponse(new Result(new Data(new FundData(
                                null, fund.title(), null, null, null, null, null, null, null, null
                        ))));
                    }
                })
                .map(r -> r.result().data().sanityFund())
                .collect(Collectors.toList());
    }
}
