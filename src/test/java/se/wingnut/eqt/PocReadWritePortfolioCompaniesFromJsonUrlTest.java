package se.wingnut.eqt;


import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import se.wingnut.eqt.domain.PortfolioCompany;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;



public class PocReadWritePortfolioCompaniesFromJsonUrlTest {

    String path = "$.result.data.allSanityCompanyPage.nodes";

    @Disabled("This tests is more of a PoC, run manually if needed")
    @Test
    void loadParseAndStore() throws IOException {
        String currentPortfolioUrl = "https://eqtgroup.com/page-data/current-portfolio/page-data.json";
        String currentPortfolioUrlTmpFilePath = "src/test/resources/download/portfolio-companies.json";

        FileUtils.copyURLToFile(new URL(currentPortfolioUrl), new File(currentPortfolioUrlTmpFilePath));

        // Create PipelineOptions
        PipelineOptions options = PipelineOptionsFactory.create();

        // Create the Pipeline
        Pipeline pipeline = Pipeline.create(options);

        // Download JSON
        PCollection<String> jsonString = pipeline.apply("Read JSON directly from API to avoid scraping",
                TextIO.read().from(currentPortfolioUrlTmpFilePath));

        PCollection<PortfolioCompany> portfolioCompanies = jsonString
                .apply("Extract with JsonPath", ParDo.of(new ExtractRecordsDoFn(path)));

        portfolioCompanies
                .apply("Serialize records back into JSON",
                        MapElements.into(TypeDescriptors.strings())
                                .via((SerializableFunction<PortfolioCompany, String>) new PortfolioCompanyToJsonFn()))
                .apply("Write JSON to resulting file using compression",
                TextIO.write().to("src/test/resources/output/portfolio-companies.json")
//                        .withCompression(Compression.GZIP) // Adds the suffix .gz to the above filename
                        .withoutSharding());

        pipeline.run().waitUntilFinish();
    }

    public static class ExtractRecordsDoFn extends DoFn<String, PortfolioCompany> {

        private final String path;

        public ExtractRecordsDoFn(String path) {
            this.path = path;
        }

        @ProcessElement
        public void processElement(ProcessContext c) {
            // Get the array as a List
            List<Object> elements = JsonPath.read(c.element(), path);

            Gson gson = new Gson();
            // Loop through each element and convert to java records
            for (Object element : elements) {
                String pcJson = gson.toJson(element);
                PortfolioCompany pc = gson.fromJson(pcJson, PortfolioCompany.class);
                c.output(pc);
            }
        }
    }

    static class PortfolioCompanyToJsonFn extends SimpleFunction<PortfolioCompany, String> {
        @Override
        public String apply(PortfolioCompany portfolioCompany) {
            return new Gson().toJson(portfolioCompany);
        }
    }

}
