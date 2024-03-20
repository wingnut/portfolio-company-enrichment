package se.wingnut.eqt.pipeline.fn;

import org.apache.beam.sdk.transforms.DoFn;
import se.wingnut.eqt.domain.PortfolioCompany;

public class AddDiscriminatorValueFn extends DoFn<PortfolioCompany, PortfolioCompany> {
    private final String isDivestment;

    public AddDiscriminatorValueFn(String isDivestment) {
        this.isDivestment = isDivestment;
    }

    @ProcessElement
    public void processElement(ProcessContext ctx) {
        PortfolioCompany pc = ctx.element();
        PortfolioCompany pcWithDiscriminator = new PortfolioCompany(
                isDivestment, // <== This is all we're after, would be nice with a "wither" function for Java records
                pc._id(),
                pc.country(),
                pc.entryDate(),
                pc.exitDate(),
                pc.fund(),
                pc.path(),
                pc.promotedSdg(),
                pc.sdg(),
                pc.sector(),
                pc.title(),
                pc.topic(),
                pc.organizationData()
        );
        ctx.output(pcWithDiscriminator);
    }

}
