package se.wingnut.eqt.pipeline;

import com.google.gson.Gson;
import org.apache.beam.sdk.transforms.DoFn;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.wingnut.eqt.domain.Organization;

import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class FilterBySideInputFnTest {
    @Mock DoFn.ProcessContext mockPC;

    Organization keepMeOrg = new Organization(null, "keep-me", null, null, null, null, null, null, null, null, null);
    Organization filterMeOrg = new Organization(null, "filter-me", null, null, null, null, null, null, null, null, null);
    String keepMeOrgJson = new Gson().toJson(keepMeOrg);
    String filterMeOrgJson = new Gson().toJson(filterMeOrg);

    @Test
    void processElement_Keep() {
        new FilterBySideInputFn().processElement(mockPC, keepMeOrgJson, List.of("keep-me", "keep-me2"));
        verify(mockPC).output("""
                {"name":"keep-me"}""");
    }

    @Test
    void processElement_Filter() {
        new FilterBySideInputFn().processElement(mockPC, filterMeOrgJson, List.of("keep-me", "keep-me2"));
        // Since the org name does not match any of the titles in the "keep list", we don't expect it to be emitted via the process context's output() method
        verify(mockPC, never()).output("""
                {"name":"filter-me"}""");
    }
}
