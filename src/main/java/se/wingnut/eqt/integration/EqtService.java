package se.wingnut.eqt.integration;

import se.wingnut.eqt.domain.FundData;
import se.wingnut.eqt.domain.PortfolioCompany;
import se.wingnut.eqt.domain.pc.PortfolioCompanyData;
import se.wingnut.eqt.domain.pc.PortfolioCompanyDataRaw;
import se.wingnut.eqt.http.SimpleRestClient;
import se.wingnut.eqt.http.fund.Data;
import se.wingnut.eqt.http.fund.FundResponse;
import se.wingnut.eqt.http.fund.Result;
import se.wingnut.eqt.http.pc.PortfolioCompanyResponse;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class EqtService implements Serializable {
    public final static String FUND_DETAILS_REST_ENDPOINT = "https://eqtgroup.com/page-data%spage-data.json";
    // These just happen to be the same, could be any url
    public final static String ORG_DETAILS_REST_ENDPOINT = "https://eqtgroup.com/page-data%spage-data.json";

    private SimpleRestClient restClient = new SimpleRestClient();

    // Primarily used for testing, where we may want to mock the rest client
    public void setRestClient(SimpleRestClient restClient) {
        this.restClient = restClient;
    }

    public PortfolioCompanyData getPortfolioCompanyDetails(PortfolioCompany pc) {
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

    public List<FundData> getFundDetails(PortfolioCompany pc) {
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
