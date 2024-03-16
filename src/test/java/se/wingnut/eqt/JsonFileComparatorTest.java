package se.wingnut.eqt;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JsonFileComparatorTest {

    @Test
    void compare_ShouldBeEqual() throws IOException {
        assertTrue(JsonFileComparator.hasSameData("src/test/resources/enrich/main_data.json", "src/test/resources/enrich/main_data.json"));
    }

    @Test
    void compare_ShouldNotBeEqual() throws IOException {
        assertFalse(JsonFileComparator.hasSameData("src/test/resources/enrich/main_data.json", "src/test/resources/enrich/side_data.json"));
    }

}
