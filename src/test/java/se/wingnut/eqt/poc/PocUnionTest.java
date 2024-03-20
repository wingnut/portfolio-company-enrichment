package se.wingnut.eqt.poc;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.Flatten;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;
import org.junit.jupiter.api.Test;

public class PocUnionTest {

    @Test
    void union() {
        // Create a Pipeline object
        Pipeline pipeline = Pipeline.create();

        // Create PCollections from your data sources
        PCollection<String> fruitsPC1 = pipeline.apply(Create.of("apple", "banana"));
        PCollection<String> fruitsPC2 = pipeline.apply(Create.of("orange", "mango"));

        // Combine PCollections into a single PCollection using Flatten
        PCollection<String> allFruitsPC =
                PCollectionList.of(fruitsPC1).and(fruitsPC2)
                        .apply(Flatten.pCollections());

        PAssert.that(allFruitsPC).containsInAnyOrder("apple", "banana", "orange", "mango");

        pipeline.run();
    }


}
