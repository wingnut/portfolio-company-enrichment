package se.wingnut.eqt.pipeline.fn;

import org.apache.beam.sdk.transforms.DoFn;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.wingnut.eqt.domain.PortfolioCompany;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SelectTitleFnTest {
    @Mock DoFn.ProcessContext mockPC;

    PortfolioCompany portfolioCompany = new PortfolioCompany(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "My Company",
            null,
            null
    );
    @Test
    void processElement_PortfolioCompany() {
        when(mockPC.element()).thenReturn(portfolioCompany);
        new SelectTitleFn().processElement(mockPC);
        // For portfolio company, we use title as join "column". Note the lower case
        verify(mockPC).output("my company");
    }
}
