package se.wingnut.eqt.pipeline.fn;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.wingnut.eqt.domain.*;
import se.wingnut.eqt.domain.pc.*;
import se.wingnut.eqt.http.SimpleRestClient;
import se.wingnut.eqt.http.fund.Data;
import se.wingnut.eqt.http.fund.FundResponse;
import se.wingnut.eqt.http.fund.Result;
import se.wingnut.eqt.http.pc.PortfolioCompanyResponse;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EnrichPortfolioCompaniesFnTest {
    @Mock
    DoFn.ProcessContext mockCtx;
    @Mock
    SimpleRestClient mockRestClient;

    // Could really use some "wither" functions with all these parameters in the constructor. Or use a builder pattern.
    // https://mail.openjdk.org/pipermail/amber-spec-experts/2022-June/003461.html
    PortfolioCompany pc = new PortfolioCompany(null, null, null,
            List.of(
                    new Fund("/current-portfolio/funds/eqt-growth/", "EQT Growth"),
                    new Fund("/current-portfolio/funds/eqt-ix/", "EQT IX")
            ),
            "/current-portfolio/my-company/",
            null, null, null, "My Company", null, null
    );
    Organization org = new Organization("123", "My Company", null, null, null, null, null, null, null, null, null);
    PortfolioCompanyDataRaw pcRaw = new PortfolioCompanyDataRaw(
            new Slug("my-company"),
            "www.example.com",
            List.of(new BoardMember("Chairperson", "John Doe", new Person("some title"))),
            List.of(new RawElement(List.of(new Child("span", "Start of text,"), new Child("span", "end of text"))))
    );
    FundData eqtGrowth = new FundData("/current-portfolio/funds/eqt-growth/", "EQT Growth", null, null, null, null, null, null, null, null);
    FundData eqtIX = new FundData("/current-portfolio/funds/eqt-ix/", "EQT IX", null, null, null, null, null, null, null, null);
    FundData eqtGrowthWithoutPath = new FundData("", "EQT Growth", null, null, null, null, null, null, null, null);
    FundData eqtIXWithoutPath = new FundData(null, "EQT IX", null, null, null, null, null, null, null, null);
    List<FundData> fundData = List.of(eqtGrowth, eqtIX);
    List<FundData> fundDataWithoutPaths = List.of(eqtGrowthWithoutPath, eqtIXWithoutPath);

    EnrichedPortfolioCompany epc = new EnrichedPortfolioCompany(null, null, null, "/current-portfolio/my-company/", null, null, null, "My Company", null,
            new Organization("123", "My Company", null, null, null, null, null, null, null, null, null),
            new PortfolioCompanyData(
                    new Slug("my-company"),
                    "www.example.com",
                    List.of(new BoardMember("Chairperson", "John Doe", new Person("some title"))),
                    "Start of text, end of text"),
            fundData
    );

    EnrichedPortfolioCompany epcWithoutFundPaths = new EnrichedPortfolioCompany(null, null, null, "/current-portfolio/my-company/", null, null, null, "My Company", null,
            new Organization("123", "My Company", null, null, null, null, null, null, null, null, null),
            new PortfolioCompanyData(
                    new Slug("my-company"),
                    "www.example.com",
                    List.of(new BoardMember("Chairperson", "John Doe", new Person("some title"))),
                    "Start of text, end of text"),
            fundDataWithoutPaths
    );

    KV<String, KV<PortfolioCompany, Organization>> pair = KV.of("My Company", KV.of(pc, org));

    @Test
    void processElement_EnrichPortfolioCompany() {
        when(mockCtx.element()).thenReturn(pair);
        when(mockRestClient.get("https://eqtgroup.com/page-data/current-portfolio/my-company/page-data.json", PortfolioCompanyResponse.class))
                .thenReturn(new PortfolioCompanyResponse(new se.wingnut.eqt.http.pc.Result(new se.wingnut.eqt.http.pc.Data(pcRaw))));
        when(mockRestClient.get("https://eqtgroup.com/page-data/current-portfolio/funds/eqt-growth/page-data.json", FundResponse.class))
                .thenReturn(new FundResponse(new Result(new Data(eqtGrowth))));
        when(mockRestClient.get("https://eqtgroup.com/page-data/current-portfolio/funds/eqt-ix/page-data.json", FundResponse.class))
                .thenReturn(new FundResponse(new Result(new Data(eqtIX))));

        EnrichPortfolioCompaniesFn fn = new EnrichPortfolioCompaniesFn();
        fn.setRestClient(mockRestClient);
        fn.processElement(mockCtx);
        verify(mockCtx).output(epc);
    }

    @Test
    void processElement_EnrichPortfolioCompany_fundWithoutPath() {
        when(mockCtx.element()).thenReturn(pair);
        when(mockRestClient.get("https://eqtgroup.com/page-data/current-portfolio/my-company/page-data.json", PortfolioCompanyResponse.class))
                .thenReturn(new PortfolioCompanyResponse(new se.wingnut.eqt.http.pc.Result(new se.wingnut.eqt.http.pc.Data(pcRaw))));
        when(mockRestClient.get("https://eqtgroup.com/page-data/current-portfolio/funds/eqt-growth/page-data.json", FundResponse.class))
                .thenReturn(new FundResponse(new Result(new Data(eqtGrowthWithoutPath))));
        when(mockRestClient.get("https://eqtgroup.com/page-data/current-portfolio/funds/eqt-ix/page-data.json", FundResponse.class))
                .thenReturn(new FundResponse(new Result(new Data(eqtIXWithoutPath))));

        EnrichPortfolioCompaniesFn fn = new EnrichPortfolioCompaniesFn();
        fn.setRestClient(mockRestClient);
        fn.processElement(mockCtx);
        verify(mockCtx).output(epcWithoutFundPaths);
    }

}
