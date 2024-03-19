package se.wingnut.eqt;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.Compression;
import se.wingnut.eqt.pipeline.EnrichPortfolioCompaniesPipelineFactory;
import se.wingnut.eqt.pipeline.PipelineCfg;
import se.wingnut.eqt.util.Decompressor;
import se.wingnut.eqt.util.Downloader;
import se.wingnut.eqt.util.UrlFilePair;

public class EqtApp {
    public static final String PORTFOLIO_FROM_WEB = "tmp/current-portfolio-from-web.json";
    public static final String DIVESTMENTS_FROM_WEB = "tmp/divestments-from-web.json";
    public static final String FUNDS_FROM_WEB = "tmp/funds-from-web.json";
    public static final String ENRICHMENT_FUNDS_FROM_GCP = "tmp/enrichment-funds-from-gcp.json.gz";
    public static final String ENRICHMENT_ORGS_FROM_GCP = "tmp/enrichment-orgs-from-web.json.gz";
    public static final String ENRICHMENT_FUNDS_FROM_GCP_UNCOMPRESSED = "tmp/enrichment-funds-from-gcp.json";
    public static final String ENRICHMENT_ORGS_FROM_GCP_UNCOMPRESSED = "tmp/enrichment-orgs-from-web.json";
    public static final String FINAL_ENRICHED_PORTFOLIO_FILE = "tmp/final-enriched-portfolio-companies.json";

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("download")) {
            System.out.println("Downloading/scraping web for portfolio data and GCP for additional org/fund data");
            // Download needed files from web/GCP in parallel
            new Downloader().downloadFiles(new UrlFilePair[]{
                    new UrlFilePair("https://eqtgroup.com/page-data/current-portfolio/page-data.json", PORTFOLIO_FROM_WEB),
                    new UrlFilePair("https://eqtgroup.com/page-data/current-portfolio/divestments/page-data.json", DIVESTMENTS_FROM_WEB),
                    new UrlFilePair("https://eqtgroup.com/page-data/current-portfolio/funds/page-data.json", FUNDS_FROM_WEB),
                    new UrlFilePair("https://storage.googleapis.com/motherbrain-external-test/interview-test-funding.json.gz", ENRICHMENT_FUNDS_FROM_GCP),
                    new UrlFilePair("https://storage.googleapis.com/motherbrain-external-test/interview-test-org.json.gz", ENRICHMENT_ORGS_FROM_GCP)
            });

            // Decompress the compressed files
            // TODO Parallelize if memory allows...
            Decompressor d = new Decompressor();
            d.decompress(ENRICHMENT_FUNDS_FROM_GCP, ENRICHMENT_FUNDS_FROM_GCP_UNCOMPRESSED);
            d.decompress(ENRICHMENT_ORGS_FROM_GCP, ENRICHMENT_ORGS_FROM_GCP_UNCOMPRESSED);
        } else {
            System.out.println("SKIPPING Downloading/scraping web for portfolio data and GCP for additional org/fund data, expecting the files to already have been downloaded and uncompressed...");
        }

        // Set up the configuration of the pipeline using the downloaded json files
        PipelineCfg cfg = new PipelineCfg(
                new PipelineCfg.PipelineFile(PORTFOLIO_FROM_WEB, Compression.UNCOMPRESSED),
                new PipelineCfg.PipelineFile(DIVESTMENTS_FROM_WEB, Compression.UNCOMPRESSED),
                new PipelineCfg.PipelineFile(FUNDS_FROM_WEB, Compression.UNCOMPRESSED),
                new PipelineCfg.PipelineFile(ENRICHMENT_FUNDS_FROM_GCP_UNCOMPRESSED, Compression.UNCOMPRESSED),
                new PipelineCfg.PipelineFile(ENRICHMENT_ORGS_FROM_GCP_UNCOMPRESSED, Compression.UNCOMPRESSED),
                new PipelineCfg.PipelineFile(FINAL_ENRICHED_PORTFOLIO_FILE, Compression.GZIP)
        );

        Pipeline pipeline = new EnrichPortfolioCompaniesPipelineFactory().createPipeline(cfg);
        System.out.println("Starting enrichment pipeline, expect to wait for a minute or two");
        pipeline.run().waitUntilFinish();
        System.out.println("Enrichment pipeline finished, check: " + cfg.finalEnrichedPortfolioCompaniesFile().url() + ".gz" + " for the result (if running with compression for the output file, .json only o/w).");
    }
}