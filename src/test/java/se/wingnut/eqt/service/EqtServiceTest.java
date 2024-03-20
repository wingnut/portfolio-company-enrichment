package se.wingnut.eqt.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.wingnut.eqt.domain.Fund;
import se.wingnut.eqt.domain.FundData;
import se.wingnut.eqt.domain.PortfolioCompany;
import se.wingnut.eqt.domain.pc.*;
import se.wingnut.eqt.http.SimpleRestClient;
import se.wingnut.eqt.http.fund.Data;
import se.wingnut.eqt.http.fund.FundResponse;
import se.wingnut.eqt.http.fund.Result;
import se.wingnut.eqt.http.pc.PortfolioCompanyResponse;
import se.wingnut.eqt.integration.EqtService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class EqtServiceTest {

    @Mock SimpleRestClient mockRestClient;

    PortfolioCompany pc = new PortfolioCompany(null, null, null, null, null,
            List.of(new Fund("/current-portfolio/funds/eqt-growth/", "EQT Growth"), new Fund("" /*NOTE, fund has no path*/, "EQT IX")),
            "/current-portfolio/my-company/",
            null, null, null, null, null, null);

    @Test
    void getPortfolioCompanyDetails() {
        PortfolioCompanyDataRaw pcRaw = new PortfolioCompanyDataRaw(
                new Slug("my-company"),
                "www.example.com",
                List.of(new BoardMember("Chairperson", "John Doe", new Person("some title"))),
                List.of(new RawElement(List.of(new Child("span", "Start of text,"), new Child("span", "end of text"))))
        );

        when(mockRestClient.get("https://eqtgroup.com/page-data/current-portfolio/my-company/page-data.json", PortfolioCompanyResponse.class))
                .thenReturn(new PortfolioCompanyResponse(new se.wingnut.eqt.http.pc.Result(new se.wingnut.eqt.http.pc.Data(pcRaw))));

        EqtService eqtService = new EqtService();
        eqtService.setRestClient(mockRestClient);

        PortfolioCompanyData actualPCD = eqtService.getPortfolioCompanyDetails(pc);

        PortfolioCompanyData expectedPCD = new PortfolioCompanyData(
                new Slug("my-company"),
                "www.example.com",
                List.of(new BoardMember("Chairperson", "John Doe", new Person("some title"))),
                "Start of text, end of text"
        );

        assertEquals(expectedPCD, actualPCD);
    }

    @Test
    void getFundDetails() {
        FundData eqtGrowth = new FundData("/current-portfolio/funds/eqt-growth/", "EQT Growth", null, null, null, null, null, null, null, null);
        FundData eqtIXWithoutPath = new FundData(null, "EQT IX", null, null, null, null, null, null, null, null);

        when(mockRestClient.get("https://eqtgroup.com/page-data/current-portfolio/funds/eqt-growth/page-data.json", FundResponse.class))
                .thenReturn(new FundResponse(new Result(new Data(eqtGrowth))));

        EqtService eqtService = new EqtService();
        eqtService.setRestClient(mockRestClient);

        List<FundData> actualFD = eqtService.getFundDetails(pc);

        // Since EXT IX has no path (well, in this test data at least) we should never call the service
        verify(mockRestClient, never()).get("https://eqtgroup.com/page-data/current-portfolio/funds/eqt-ix/page-data.json", FundResponse.class);

        List expectedFD = List.of(eqtGrowth, eqtIXWithoutPath);
        assertEquals(expectedFD, actualFD);
    }

}
