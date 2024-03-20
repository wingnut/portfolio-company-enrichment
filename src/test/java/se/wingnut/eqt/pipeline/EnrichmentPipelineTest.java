package se.wingnut.eqt.pipeline;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.Compression;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import se.wingnut.eqt.JsonFileComparator;
import se.wingnut.eqt.util.Downloader;
import se.wingnut.eqt.util.UrlFilePair;

import java.io.IOException;

import static se.wingnut.eqt.EqtApp.*;

public class EnrichmentPipelineTest {

    /**
     * Here we test the pipeline e2e and verify the output against a known static subset of the prod data.
     * NOTE: This will cause actual calls to GET additional fund data, so it depends on the EQT website being up.
     * In a real life scenario, I'd have spent time on setting up a mock website to emulate EQT.
     */
    @Test
    void enrich() throws IOException {
        PipelineCfg cfg = new PipelineCfg(
                new PipelineCfg.PipelineFile("src/test/resources/enrich/portfolio-companies-short.json", Compression.UNCOMPRESSED),
                new PipelineCfg.PipelineFile("not-used", Compression.UNCOMPRESSED),
                new PipelineCfg.PipelineFile("not-used", Compression.UNCOMPRESSED),
                new PipelineCfg.PipelineFile("not used", Compression.UNCOMPRESSED),
                new PipelineCfg.PipelineFile("src/test/resources/enrich/organizations-reference-data-short.json", Compression.UNCOMPRESSED),
                new PipelineCfg.PipelineFile("src/test/resources/output/final-enriched-data.json", Compression.UNCOMPRESSED)
        );

        Pipeline pipeline = new EnrichPortfolioCompaniesPipelineFactory().createPipeline(cfg);
        pipeline.run().waitUntilFinish();

        // Verify output is as expected
        Assertions.assertTrue(JsonFileComparator.hasSameData("src/test/resources/enrich/expected-final-enriched-data.json", "src/test/resources/output/final-enriched-data.json"));
    }

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

    @Disabled("Can be used to run the enrichment pipeline from a test rather than via the main method. Disabled here as the cfg is using prod files which are too large to serve as a unit test")
    @Test
    void enrichProdData() {
        PipelineCfg cfg = new PipelineCfg(
                new PipelineCfg.PipelineFile(PORTFOLIO_FROM_WEB, Compression.UNCOMPRESSED),
                new PipelineCfg.PipelineFile(DIVESTMENTS_FROM_WEB, Compression.UNCOMPRESSED),
                new PipelineCfg.PipelineFile(FUNDS_FROM_WEB, Compression.UNCOMPRESSED),
                new PipelineCfg.PipelineFile(ENRICHMENT_FUNDS_FROM_GCP_UNCOMPRESSED, Compression.UNCOMPRESSED),
                new PipelineCfg.PipelineFile(ENRICHMENT_ORGS_FROM_GCP_UNCOMPRESSED, Compression.UNCOMPRESSED),
                // Typically using uncompressed here when debugging, prod uses GZIP for the output file
                new PipelineCfg.PipelineFile(FINAL_ENRICHED_PORTFOLIO_FILE, Compression.UNCOMPRESSED)
        );

        Pipeline pipeline = new EnrichPortfolioCompaniesPipelineFactory().createPipeline(cfg);
        pipeline.run().waitUntilFinish();
    }

}
