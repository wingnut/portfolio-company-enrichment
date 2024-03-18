package se.wingnut.eqt;

import com.google.gson.Gson;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.SerializableCoder;
import org.apache.beam.sdk.extensions.joinlibrary.Join;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertTrue;

// Assuming you have classes for User, Transaction, JoinedData

public class PocEnrichDataPipelineJoinTest {

    interface HasId {
        String getId();
    }

    record MainData(String id, String name) implements HasId, Serializable {
        @Override
        public String getId() {
            return id;
        }
    }

    record SideData(String id, String phone) implements HasId, Serializable {
        @Override
        public String getId() {
            return id;
        }
    }

    record EnrichedData(String id, String name, String phone) implements HasId, Serializable {
        @Override
        public String getId() {
            return id;
        }
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

        // Key by the common join column
        PCollection<KV<String, MainData>> keyedUsers = parsedMainData
                .apply("Key MainData by id", ParDo.of(new KeyFn<>()));

        PCollection<KV<String, SideData>> keyedTransactions = parsedSideData
                .apply("Key SideData by id", ParDo.of(new KeyFn<>()));

        // Inner join using Join.innerJoin or leftOuterJoin? leftOuterJoin seems most appropriate since we want to keep all companies even if they have no data to enrich with
        PCollection<KV<String, KV<MainData, SideData>>> joinedData = Join.leftOuterJoin(keyedUsers, keyedTransactions, new SideData(null, null));

        joinedData.apply("Serialize records back into JSON",
                        MapElements.into(TypeDescriptors.strings())
                                .via((SerializableFunction<KV<String, KV<MainData, SideData>>, String>) new JoinedToJsonFn()))
                .apply("Write JSON to resulting file and compress",
                        TextIO.write().to("src/test/resources/output/joined.json")
                                .withoutSharding()); // Produce one output file only here in local env

        pipeline.run().waitUntilFinish();

        assertTrue(JsonFileComparator.hasSameData("src/test/resources/enrich/expected.json", "src/test/resources/output/joined.json"));
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

    static class KeyFn<T extends HasId> extends DoFn<T, KV<String, T>> {
        @ProcessElement
        public void processElement(ProcessContext c) {
            T element = c.element();
            String keyValue = element.getId();
            c.output(KV.of(keyValue, element));
        }
    }

    static class JoinedToJsonFn extends SimpleFunction<KV<String, KV<MainData, SideData>>, String> {
        @Override
        public String apply(KV<String, KV<MainData, SideData>> joined) {
            String id = joined.getKey();
            MainData mainData = joined.getValue().getKey();
            SideData sideData = joined.getValue().getValue();
            EnrichedData enriched = new EnrichedData(id, mainData.name(), sideData.phone());
            return new Gson().toJson(enriched);
        }
    }

}
