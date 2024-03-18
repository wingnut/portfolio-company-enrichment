package se.wingnut.eqt.pipeline;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.wingnut.eqt.domain.Organization;
import se.wingnut.eqt.domain.PortfolioCompany;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LowerCaseKeyFnTest {
    @Mock DoFn.ProcessContext mockPC;

    Organization organization = new Organization(null, "My Company", null, null, null, null, null, null, null, null, null);
    PortfolioCompany portfolioCompany = new PortfolioCompany(null, null, null, null, null, null, null, null, "My Company", null, null);

    @Test
    void processElement_Organization() {
        when(mockPC.element()).thenReturn(organization);
        new LowerCaseKeyFn<Organization>().processElement(mockPC);
        // For org, we use name LOWER CASED as join "column"
        verify(mockPC).output(KV.of(organization.name().toLowerCase(), organization));
    }

    @Test
    void processElement_PortfolioCompany() {
        when(mockPC.element()).thenReturn(organization);
        new LowerCaseKeyFn<PortfolioCompany>().processElement(mockPC);
        // For portfolio company, we use title LOWER CASED as join "column"
        verify(mockPC).output(KV.of(portfolioCompany.title().toLowerCase(), organization));
    }
}
