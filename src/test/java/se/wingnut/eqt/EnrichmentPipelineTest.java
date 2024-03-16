package se.wingnut.eqt;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.Compression;
import org.junit.jupiter.api.Test;
import se.wingnut.eqt.pipeline.EnrichPortfolioCompaniesPipelineFactory;

public class EnrichmentPipelineTest {
    @Test
    void enrich() {
        Pipeline pipeline = new EnrichPortfolioCompaniesPipelineFactory().createPipeline(
                "src/test/resources/enrich/portfolio-companies-short.json", Compression.UNCOMPRESSED,
                "src/test/resources/enrich/organizations-reference-data-short.json", Compression.UNCOMPRESSED);

        pipeline.run().waitUntilFinish();
    }
}
