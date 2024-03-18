package se.wingnut.eqt.pipeline.fn;

import org.apache.beam.sdk.transforms.DoFn;
import se.wingnut.eqt.domain.PortfolioCompany;

public class SelectTitleFn extends DoFn<PortfolioCompany, String> {
    @ProcessElement
    public void processElement(ProcessContext c) {
        c.output(c.element().title().toLowerCase());
    }
}
