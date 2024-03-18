package se.wingnut.eqt.pipeline;

import org.apache.beam.sdk.transforms.DoFn;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.wingnut.eqt.domain.Organization;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParseJsonFnTest {
    @Mock DoFn.ProcessContext mockPC;

    Organization organization = new Organization("some-long-uuid", "My Company", null, null, null, null, null, null, null, null, null);

    @Test
    void processElement_ParseOrganization() {
        when(mockPC.element()).thenReturn("{\"uuid\": \"some-long-uuid\", \"name\": \"My Company\"}");
        new ParseJsonFn<>(Organization.class).processElement(mockPC);
        // For org, we use name as join "column"
        verify(mockPC).output(organization);
    }

}
