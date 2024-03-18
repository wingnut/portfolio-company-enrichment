package se.wingnut.eqt.pipeline.fn;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import org.apache.beam.sdk.transforms.DoFn;
import se.wingnut.eqt.domain.PortfolioCompany;

import java.util.List;

// TODO Make generic
public class ExtractPortfolioCompanyElementsFromPathDoFn extends DoFn<String, PortfolioCompany> {

    private final String path;

    public ExtractPortfolioCompanyElementsFromPathDoFn(String path) {
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

