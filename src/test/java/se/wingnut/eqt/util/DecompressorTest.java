package se.wingnut.eqt.util;

import org.junit.jupiter.api.Test;
import se.wingnut.eqt.JsonFileComparator;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DecompressorTest {

    @Test
    void decompress() throws IOException {
        new Decompressor().decompress("src/test/resources/decompress/some.json.gz", "src/test/resources/output/some.json");
        assertTrue(JsonFileComparator.hasSameData("src/test/resources/decompress/expected.json", "src/test/resources/output/some.json"));
    }

}
