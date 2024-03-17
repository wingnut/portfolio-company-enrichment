package se.wingnut.eqt;

import org.apache.beam.sdk.Pipeline;
import se.wingnut.eqt.pipeline.EnrichPortfolioCompaniesPipelineFactory;
import se.wingnut.eqt.util.Downloader;
import se.wingnut.eqt.util.UrlFilePair;

public class Main {
    public static final String PORTFOLIO_FROM_WEB = "tmp/current-portfolio-from-web.json";
    public static final String DIVESTMENTS_FROM_WEB = "tmp/divestments-from-web.json";
    public static final String FUNDS_FROM_WEB = "tmp/funds-from-web.json";
    public static final String ENRICHMENT_FUNDS_FROM_GCP = "tmp/enrichment-funds-from-gcp.json.gz";
    public static final String ENRICHMENT_ORGS_FROM_WEB = "tmp/enrichment-orgs-from-web.json.gz";
    public static final String FINAL_ENRICHED_PORTFOLIO_FILE = "tmp/enrichedPortfolioCompanies.json";

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("download")) {
            System.out.println("Downloading/scraping web for portfolio data and GCP for additional org/fund data");
            // Download needed files from web/GCP in parallel
            new Downloader().downloadFiles(new UrlFilePair[]{
                    new UrlFilePair("https://eqtgroup.com/page-data/current-portfolio/page-data.json", PORTFOLIO_FROM_WEB),
                    new UrlFilePair("https://eqtgroup.com/page-data/current-portfolio/divestments/page-data.json", DIVESTMENTS_FROM_WEB),
                    new UrlFilePair("https://eqtgroup.com/page-data/current-portfolio/funds/page-data.json", FUNDS_FROM_WEB),
                    new UrlFilePair("https://storage.googleapis.com/motherbrain-external-test/interview-test-funding.json.gz", ENRICHMENT_FUNDS_FROM_GCP),
                    new UrlFilePair("https://storage.googleapis.com/motherbrain-external-test/interview-test-org.json.gz", ENRICHMENT_ORGS_FROM_WEB)
            });
        }

        Pipeline pipeline = new EnrichPortfolioCompaniesPipelineFactory().createPipeline();

        pipeline.run().waitUntilFinish();
    }
}