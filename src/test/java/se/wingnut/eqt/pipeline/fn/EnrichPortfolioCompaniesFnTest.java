package se.wingnut.eqt.pipeline.fn;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.wingnut.eqt.domain.*;
import se.wingnut.eqt.http.SimpleRestClient;
import se.wingnut.eqt.http.fund.Data;
import se.wingnut.eqt.http.fund.FundResponse;
import se.wingnut.eqt.http.fund.Result;

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
            null, null, null, null, "My Company", null, null);
    Organization org = new Organization("123", "My Company", null, null, null, null, null, null, null, null, null);
    FundData eqtGrowth = new FundData("/current-portfolio/funds/eqt-growth/", "EQT Growth", null, null, null, null, null, null, null, null);
    FundData eqtIX = new FundData("/current-portfolio/funds/eqt-ix/", "EQT IX", null, null, null, null, null, null, null, null);
    List<FundData> fundData = List.of(eqtGrowth, eqtIX);
    EnrichedPortfolioCompany epc = new EnrichedPortfolioCompany(null, null, null, null, null, null, null, "My Company", null,
            new Organization("123", "My Company", null, null, null, null, null, null, null, null, null),
            fundData);

    KV<String, KV<PortfolioCompany, Organization>> pair = KV.of("My Company", KV.of(pc, org));

    @Test
    void processElement_EnrichPortfolioCompany() {
        when(mockCtx.element()).thenReturn(pair);
        when(mockRestClient.get("https://eqtgroup.com/page-data/current-portfolio/funds/eqt-growth/page-data.json", FundResponse.class))
                .thenReturn(new FundResponse(new Result(new Data(eqtGrowth))));
        when(mockRestClient.get("https://eqtgroup.com/page-data/current-portfolio/funds/eqt-ix/page-data.json", FundResponse.class))
                .thenReturn(new FundResponse(new Result(new Data(eqtIX))));

        EnrichPortfolioCompaniesFn fn = new EnrichPortfolioCompaniesFn();
        fn.setRestClient(mockRestClient);
        fn.processElement(mockCtx);
        verify(mockCtx).output(epc);
    }

}
