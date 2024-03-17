package se.wingnut.eqt;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionView;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import se.wingnut.eqt.domain.Organization;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PocFilterPCollectionTest {

//    // Define a PCollection of elements with IDs (replace with your actual data)
//    PCollection<Element> elements = ...;
//
//    // Define a PCollection of IDs to filter by (replace with your actual data)
//    PCollection<String> filterIds = ...;
//
//    // Create a Broadcast variable to hold the filter IDs efficiently
//    Broadcast<Iterable<String>> broadcastIds = elements.getPipeline().apply("BroadcastFilterIds", Broadcast.create(filterIds.asSet()));
//
//    // Apply Filter with a custom predicate that checks against the broadcast IDs
//    PCollection<Element> filteredElements = elements.apply("FilterByIds", Filter.withPredicate(element ->
//            broadcastIds.get().contains(element.getId())));

    @Disabled("This tests is more of a PoC, run manually if needed")
    @Test
    void loadParseFilterAndStore() throws IOException {
        PipelineOptions options = PipelineOptionsFactory.create();
        Pipeline pipeline = Pipeline.create(options);

        PCollection<Organization> allOrgs = pipeline.apply("Read JSON file",
                        TextIO.read().from("src/test/resources/filter/orgs.json"))
                .apply("Parse JSON into java records",
                        MapElements.into(TypeDescriptors.strings())
                                .via(new PocReadWriteOrganizationsAsCompressedJsonFileTest.OrganizationFromJsonFn()));

        // FILTER on some other collection of IDs, in this case the company names
        PCollection<String> filterIds = pipeline.apply("CreateIds", Create.of("Biotronics", "Bi Studio®", "Saraya"));
        // Create a PCollectionView from filterIds
        PCollectionView<List<String>> filterIdsView = filterIds.apply(View.asList());

        PCollection<Organization> filteredOrgs = allOrgs
                .apply("Filter by ID",
                        ParDo.of(new FilterBySideInputFn()).withSideInput("filterIdsView", filterIdsView));

        filteredOrgs.apply("Serialize records back into JSON",
                        MapElements.into(TypeDescriptors.strings())
                                .via((SerializableFunction<Organization, String>) new PocReadWriteOrganizationsAsCompressedJsonFileTest.OrganizationToJsonFn()))
                .apply("Write JSON to resulting filtered file",
                        TextIO.write().to("src/test/resources/output/filtered-orgs.json")
                                .withoutSharding()); // Produce one output file only here in local env

        pipeline.run().waitUntilFinish();

        // Verify results
        assertTrue(JsonFileComparator.hasSameData("src/test/resources/filter/expected.json", "src/test/resources/output/filtered-orgs.json"));
    }

    static class FilterBySideInputFn extends DoFn<Organization, Organization> {
        @ProcessElement
        public void processElement(ProcessContext c, @Element Organization org, @SideInput("filterIdsView") List<String> filterIdsView) {
            if (filterIdsView.contains(org.name())) {
                c.output(org);
            }
        }
    }
}
