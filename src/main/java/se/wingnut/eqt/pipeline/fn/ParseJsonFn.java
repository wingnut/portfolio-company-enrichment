package se.wingnut.eqt.pipeline.fn;

import com.google.gson.Gson;
import org.apache.beam.sdk.transforms.DoFn;

/**
 * Generic Apache Beam DoFn for parsing json into java classes/records
 * @param <T>
 */
public class ParseJsonFn<T> extends DoFn<String, T> {
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
