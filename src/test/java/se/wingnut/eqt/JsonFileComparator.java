package se.wingnut.eqt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class JsonFileComparator {

    public static boolean hasSameData(String filePath1, String filePath2) throws IOException {
        List<String> lines1 = Files.lines(Paths.get(filePath1)).collect(Collectors.toList());
        List<String> lines2 = Files.lines(Paths.get(filePath2)).collect(Collectors.toList());

        lines1.sort(Comparator.naturalOrder());
        lines2.sort(Comparator.naturalOrder());

        return lines1.equals(lines2);
    }

}
