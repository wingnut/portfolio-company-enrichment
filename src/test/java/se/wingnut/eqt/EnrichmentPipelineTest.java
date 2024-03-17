package se.wingnut.eqt;

import org.apache.beam.sdk.Pipeline;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import se.wingnut.eqt.pipeline.EnrichPortfolioCompaniesPipelineFactory;
import se.wingnut.eqt.util.Downloader;
import se.wingnut.eqt.util.UrlFilePair;

public class EnrichmentPipelineTest {

    @Disabled("For testing purposes we sometimes want to download the files first, but if they are already downloaded we can skip this part")
    @Test
    void downloadFiles() {
        new Downloader().downloadFiles(new UrlFilePair[]{
                new UrlFilePair("https://eqtgroup.com/page-data/current-portfolio/page-data.json", "tmp/current-portfolio-from-web.json"),
                new UrlFilePair("https://eqtgroup.com/page-data/current-portfolio/divestments/page-data.json", "tmp/divestments-from-web.json"),
                new UrlFilePair("https://eqtgroup.com/page-data/current-portfolio/funds/page-data.json", "tmp/funds-from-web.json"),
                new UrlFilePair("https://storage.googleapis.com/motherbrain-external-test/interview-test-funding.json.gz", "tmp/enrichment-funds-from-gcp.json.gz"),
                new UrlFilePair("https://storage.googleapis.com/motherbrain-external-test/interview-test-org.json.gz", "tmp/enrichment-orgs-from-web.json.gz")
        });
    }

    @Disabled()
    @Test
    void enrich() {
        Pipeline pipeline = new EnrichPortfolioCompaniesPipelineFactory().createPipeline();
        pipeline.run().waitUntilFinish();
    }
}
