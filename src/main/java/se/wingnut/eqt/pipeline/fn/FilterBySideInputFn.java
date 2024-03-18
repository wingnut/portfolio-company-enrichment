package se.wingnut.eqt.pipeline.fn;

import com.google.gson.Gson;
import org.apache.beam.sdk.transforms.DoFn;
import se.wingnut.eqt.domain.Organization;

import java.util.List;

public class FilterBySideInputFn extends DoFn<String, String> {
    @ProcessElement
    public void processElement(ProcessContext c, @Element String org, @SideInput("titleFilterView") List<String> titleFilter) {
        Organization o = new Gson().fromJson(org, Organization.class);
        // No guarantees but better overall success rate using case-insensitive matching
        if (titleFilter.contains(o.name().toLowerCase())) {
            c.output(org);
        }
    }
}
