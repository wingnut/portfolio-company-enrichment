package se.wingnut.eqt.poc;

import com.google.gson.Gson;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.Compression;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import se.wingnut.eqt.domain.Funding;

public class PocReadWriteFundingsAsCompressedJsonFileTest {

    @Disabled("This tests is more of a PoC, run manually if needed")
    @Test
    void loadParseAndStore() {
        // Create PipelineOptions
        PipelineOptions options = PipelineOptionsFactory.create();

        // Create the Pipeline
        Pipeline pipeline = Pipeline.create(options);

        // Read JSON.gz file
        pipeline.apply("Read compressed JSON file",
                        TextIO.read().from("src/test/resources/short/interview-test-funding-short.json.gz")
                                .withCompression(Compression.GZIP))

                // Parse JSON strings into Java records
                .apply("Parse JSON into records",
                        MapElements.into(TypeDescriptors.strings())
                                .via(new FundingFromJsonFn()))

                // Do your thing here!

                .apply("Serialize records back into JSON",
                        MapElements.into(TypeDescriptors.strings())
                        .via((SerializableFunction<Funding, String>) new FundingToJsonFn()))

                // Run the Pipeline
                .apply("Write JSON to resulting file and compress",
                        TextIO.write().to("src/test/resources/output/interview-test-funding-short.json")
                                .withCompression(Compression.GZIP) // Adds the suffix .gz to the above filename
                                .withoutSharding()); // Produce one output file only here in local env

        pipeline.run().waitUntilFinish();

        // TODO Read the contents of both original and produced file and compare contents
    }

    static class FundingFromJsonFn extends SimpleFunction<String, Funding> {
        @Override
        public Funding apply(String json) {
            return new Gson().fromJson(json, Funding.class);
        }
    }

    static class FundingToJsonFn extends SimpleFunction<Funding, String> {
        @Override
        public String apply(Funding funding) {
            return new Gson().toJson(funding);
        }
    }

}
