package se.wingnut.eqt.util;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class DownloaderTest {

    @Disabled("We don't worry to much about unit testing this workaround for local dev (DirectRunners don't support http downloads)")
    @Test
    void downloadFile() {
        new Downloader().downloadFile(new UrlFilePair("\n" +
                "https://eqtgroup.com/page-data/current-portfolio/funds/page-data.json",
                "tmp/funds-from-web.json"));
    }

    @Disabled("We don't worry to much about unit testing this workaround for local dev (DirectRunners don't support http downloads)")
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

}
