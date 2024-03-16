package se.wingnut.eqt;

import org.apache.beam.sdk.Pipeline;
import se.wingnut.eqt.pipeline.EnrichPortfolioCompaniesPipelineFactory;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("download")) {
            System.out.println("Downloading/scraping web for portfolio data and GCP for additional org/fund data");
            // TODO Download from web/GCP in parallel
        }

        Pipeline pipeline = new EnrichPortfolioCompaniesPipelineFactory().createPipeline();
        pipeline.run().waitUntilFinish();
    }
}