package se.wingnut.eqt.pipeline;

import org.apache.beam.sdk.transforms.DoFn;
import se.wingnut.eqt.domain.PortfolioCompany;

class SelectTitleFn extends DoFn<PortfolioCompany, String> {
    @ProcessElement
    public void processElement(ProcessContext c) {
        c.output(c.element().title().toLowerCase());
    }
}
