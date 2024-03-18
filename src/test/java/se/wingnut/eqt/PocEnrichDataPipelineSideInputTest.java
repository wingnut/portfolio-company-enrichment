package se.wingnut.eqt;

import com.google.gson.Gson;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.SerializableCoder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionView;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PocEnrichDataPipelineSideInputTest {

    record MainData(String id, String name) implements Serializable {
    }

    record SideData(String id, String phone) implements Serializable {
    }

    record EnrichedData(String id, String name, String phone) implements Serializable {
    }

    @Disabled("This tests is more of a PoC, run manually if needed")
    @Test
    void enrich() throws IOException {
        Pipeline pipeline = Pipeline.create();

        // Read main JSON data
        PCollection<String> mainData = pipeline.apply("Read Main Data", TextIO.read().from("src/test/resources/enrich/main_data.json"));

        // Read side JSON data
        PCollection<String> sideData = pipeline.apply("Read Side Data", TextIO.read().from("src/test/resources/enrich/side_data.json"));

        // Parse main JSON data into MainData records
        PCollection<MainData> parsedMainData = mainData
                .apply("Parse Main Data", ParDo.of(new ParseJsonFn<>(MainData.class))).setCoder(SerializableCoder.of(MainData.class));

        // Parse side JSON data into SideData records
        PCollection<SideData> parsedSideData = sideData
                .apply("Parse Side Data", ParDo.of(new ParseJsonFn<>(SideData.class))).setCoder(SerializableCoder.of(SideData.class));

        // Convert parsedSideData into a PCollectionView
        PCollectionView<List<SideData>> sideDataView = parsedSideData.apply("CreateSideDataView", View.asList());

        // Enrich main data with side data
        PCollection<EnrichedData> enrichedDatas = parsedMainData.apply("Enrich using SideData as sideInputs",
                        ParDo.of(new EnrichDataFn(sideDataView)).withSideInputs(sideDataView)).setCoder(SerializableCoder.of(EnrichedData.class));

                // Write enriched data to output
        PCollection<String> enrichedDatasJson = enrichedDatas.apply("Serialize records back into JSON",
                        MapElements.into(TypeDescriptors.strings())
                                .via((SerializableFunction<EnrichedData, String>) new EnrichedDataToJsonFn()));

        enrichedDatasJson.apply("Write JSON to resulting file and compress",
                TextIO.write().to("src/test/resources/output/sideInput.json")
                        .withoutSharding()); // Produce one output file only here in local env

        // Run the pipeline
        pipeline.run().waitUntilFinish();

        assertTrue(JsonFileComparator.hasSameData("src/test/resources/enrich/expected.json", "src/test/resources/output/sideInput.json"));
    }

    static class ParseJsonFn<T> extends DoFn<String, T> {
        private final Class<T> clazz;

        public ParseJsonFn(Class<T> clazz) {
            this.clazz = clazz;
        }

        @ProcessElement
        public void processElement(ProcessContext c) {
            Gson gson = new Gson();
            String json = c.element();
            T record = gson.fromJson(json, clazz);
            c.output(record);
        }
    }

    static class EnrichDataFn extends DoFn<MainData, EnrichedData> {
        private final PCollectionView<List<SideData>> sideDataView;

        public EnrichDataFn(PCollectionView<List<SideData>> sideDataView) {
            this.sideDataView = sideDataView;
        }

        @ProcessElement
        public void processElement(ProcessContext c) {
            MainData mainData = c.element();
            Iterable<SideData> sideInputData = c.sideInput(sideDataView);
            Map<String, String> map = StreamSupport.stream(sideInputData.spliterator(), false)
                    .collect(Collectors.toMap(sd -> sd.id(), sd -> sd.phone));
            String id = mainData.id();

            EnrichedData enrichedData = new EnrichedData(id, mainData.name(), map.get(id));
            c.output(enrichedData);
        }
    }

    static class EnrichedDataToJsonFn extends SimpleFunction<EnrichedData, String> {
        @Override
        public String apply(EnrichedData ed) {
            return new Gson().toJson(ed);
        }
    }

}

