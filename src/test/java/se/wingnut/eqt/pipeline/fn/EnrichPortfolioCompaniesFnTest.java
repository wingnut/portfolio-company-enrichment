package se.wingnut.eqt.pipeline.fn;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.wingnut.eqt.domain.*;
import se.wingnut.eqt.domain.pc.BoardMember;
import se.wingnut.eqt.domain.pc.Person;
import se.wingnut.eqt.domain.pc.PortfolioCompanyData;
import se.wingnut.eqt.domain.pc.Slug;
import se.wingnut.eqt.integration.EqtService;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EnrichPortfolioCompaniesFnTest {
    @Mock
    DoFn.ProcessContext mockCtx;
    @Mock
    EqtService mockEqtService;

    // Could really use some "wither" functions with all these parameters in the constructor. Or use a builder pattern.
    // https://mail.openjdk.org/pipermail/amber-spec-experts/2022-June/003461.html
    PortfolioCompany pc = new PortfolioCompany("true", null, null, null, "2021-03-01",
            List.of(
                    new Fund("/current-portfolio/funds/eqt-growth/", "EQT Growth"),
                    new Fund("/current-portfolio/funds/eqt-ix/", "EQT IX")
            ),
            "/current-portfolio/my-company/",
            null, null, null, "My Company", null, null
    );
    Organization org = new Organization("123", "My Company", null, null, null, null, null, null, null, null, null);

    FundData eqtGrowth = new FundData("/current-portfolio/funds/eqt-growth/", "EQT Growth", null, null, null, null, null, null, null, null);
    FundData eqtIX = new FundData("/current-portfolio/funds/eqt-ix/", "EQT IX", null, null, null, null, null, null, null, null);
    FundData eqtGrowthWithoutPath = new FundData("", "EQT Growth", null, null, null, null, null, null, null, null);
    FundData eqtIXWithoutPath = new FundData(null, "EQT IX", null, null, null, null, null, null, null, null);
    List<FundData> fundData = List.of(eqtGrowth, eqtIX);
    List<FundData> fundDataWithoutPaths = List.of(eqtGrowthWithoutPath, eqtIXWithoutPath);

    PortfolioCompanyData portfolioCompanyData = new PortfolioCompanyData(
            new Slug("my-company"),
            "www.example.com",
            List.of(new BoardMember("Chairperson", "John Doe", new Person("some title"))),
            "Start of text, end of text"
    );

    EnrichedPortfolioCompany epc = new EnrichedPortfolioCompany("true", null, null, null, "2021-03-01", "/current-portfolio/my-company/", null, null, null, "My Company", null,
            new Organization("123", "My Company", null, null, null, null, null, null, null, null, null),
            portfolioCompanyData,
            fundData
    );

    EnrichedPortfolioCompany epcWithoutFundPaths = new EnrichedPortfolioCompany("true", null, null, null, "2021-03-01", "/current-portfolio/my-company/", null, null, null, "My Company", null,
            new Organization("123", "My Company", null, null, null, null, null, null, null, null, null),
            portfolioCompanyData,
            fundDataWithoutPaths
    );

    KV<String, KV<PortfolioCompany, Organization>> pair = KV.of("My Company", KV.of(pc, org));

    @Test
    void processElement_EnrichPortfolioCompany() {
        when(mockCtx.element()).thenReturn(pair);
        when(mockEqtService.getPortfolioCompanyDetails(pc)).thenReturn(portfolioCompanyData);
        when(mockEqtService.getFundDetails(pc)).thenReturn(fundData);

        EnrichPortfolioCompaniesFn fn = new EnrichPortfolioCompaniesFn();
        fn.setEqtService(mockEqtService);
        fn.processElement(mockCtx);
        verify(mockCtx).output(epc);
    }

    @Test
    void processElement_EnrichPortfolioCompany_fundWithoutPath() {
        when(mockCtx.element()).thenReturn(pair);
        when(mockEqtService.getPortfolioCompanyDetails(pc)).thenReturn(portfolioCompanyData);
        when(mockEqtService.getFundDetails(pc)).thenReturn(fundDataWithoutPaths);

        EnrichPortfolioCompaniesFn fn = new EnrichPortfolioCompaniesFn();
        fn.setEqtService(mockEqtService);
        fn.processElement(mockCtx);
        verify(mockCtx).output(epcWithoutFundPaths);
    }

}
