package se.wingnut.eqt.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class Decompressor {
    public void decompress(String inputFile, String outputFile) {
        System.out.println("Decompressing " + inputFile + " into " + outputFile);
        try (FileInputStream fis = new FileInputStream(inputFile);
             GZIPInputStream gis = new GZIPInputStream(fis);
             FileOutputStream fos = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("File decompressed successfully.");
    }
}

