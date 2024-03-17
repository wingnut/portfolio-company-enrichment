package se.wingnut.eqt.pipeline;

import org.apache.beam.sdk.io.Compression;

public record PipelineCfg(
        // The url to portfolio companies (scraping/downloading from the web in a real scenario, but is pre-downloaded to local file in this demo)
        PipelineFile portfolioFromWeb,
        // The url to divestments (scraping/downloading from the web in a real scenario, but is pre-downloaded to local file in this demo)
        PipelineFile divestmentsFromWeb,
        // The url to funds (scraping/downloading from the web in a real scenario, but is pre-downloaded to local file in this demo)
        PipelineFile fundsFromWeb,
        // The url to fund enrichment data (downloading from GCP-bucket in a real scenario, but is pre-downloaded to local file in this demo)
        PipelineFile enrichmentFundsFromGCP,
        // The url to org enrichment data (downloading from GCP-bucket in a real scenario, but is pre-downloaded to local file in this demo)
        PipelineFile enrichmentOrgsFromGCP,
        // This is the location/compression to use for the final result of the enrichment pipeline
        PipelineFile finalEnrichedPortfolioCompaniesFile) {
    public record PipelineFile(
            // The url (can be on the web: http(s) or a local file)
            String url,
            // The file compression type (or UNCOMPRESSED if no compression is used)
            Compression compression
    ) {}
}



