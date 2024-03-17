package se.wingnut.eqt.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;

public class Downloader {

    public void downloadFiles(UrlFilePair[] urlFilePairs) {
        CompletableFuture<Void>[] downloadTasks = new CompletableFuture[urlFilePairs.length];
        for (int i = 0; i < urlFilePairs.length; i++) {
            UrlFilePair pair = urlFilePairs[i];
            downloadTasks[i] = CompletableFuture.runAsync(() -> downloadFile(pair));
        }

        // Wait for all downloads to finish (using allOf)
        CompletableFuture<Void> allDownloads = CompletableFuture.allOf(downloadTasks);
        allDownloads.join();  // Blocks until completion of all download tasks
    }

    public void downloadFile(UrlFilePair pair) {
        try {
            long start = System.currentTimeMillis();
            System.out.println("Downloading " + pair.sourceUrl() + " to " + pair.destinationFileName());
            URL url = new URL(pair.sourceUrl());
            try (InputStream in = url.openStream()) {
                Files.copy(in, Paths.get(pair.destinationFileName()), StandardCopyOption.REPLACE_EXISTING);
            }
            long stop = System.currentTimeMillis();
            System.out.println("Download of " + pair.sourceUrl() + " to " + pair.destinationFileName() + " DONE - Took: " + (stop-start) + "ms");
        } catch (IOException e) {
            // Ignoring any more complicated error handling here as this whole download thing is a local dev thing only (DirectRunners cannot download files over http)
            throw new RuntimeException(e);
        }
    }

}
