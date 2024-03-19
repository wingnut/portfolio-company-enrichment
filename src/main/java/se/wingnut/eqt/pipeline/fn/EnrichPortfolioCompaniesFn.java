package se.wingnut.eqt.pipeline.fn;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import se.wingnut.eqt.domain.EnrichedPortfolioCompany;
import se.wingnut.eqt.domain.Organization;
import se.wingnut.eqt.domain.PortfolioCompany;

public class EnrichPortfolioCompaniesFn extends DoFn<KV<String, KV<PortfolioCompany, Organization>>, EnrichedPortfolioCompany> {
    @ProcessElement
    public void processElement(ProcessContext ctx) {
        KV<String, KV<PortfolioCompany, Organization>> pair = ctx.element();
        PortfolioCompany pc = pair.getValue().getKey();
        Organization o = pair.getValue().getValue();

        EnrichedPortfolioCompany epc = new EnrichedPortfolioCompany(
                pc._id(),
                pc.country(),
                pc.entryDate(),
                pc.fund(), // TODO Enrich with additional funds data at the next step?
                pc.path(),
                pc.promotedSdg(),
                pc.sdg(),
                pc.sector(),
                pc.title(),
                pc.topic(),
                // Enriching with the organization data, leave out any duplicate fields (already in the portfolio company JSON) by setting them to null
                new Organization(
                        o.uuid(),
                        o.name(), // Same as pc.title, so we _could_ set this to null to avoid duplicating data. However, it is just one field, so I'll keep it for easier debugging
                        o.homepage_url(),
                        o.country_code(),
                        o.city(),
                        o.founded_on(),
                        o.short_description(),
                        o.employee_count(),
                        o.num_funding_rounds(),
                        o.last_funding_on(),
                        o.total_funding_usd()
                )
        );

        ctx.output(epc);
    }
}
