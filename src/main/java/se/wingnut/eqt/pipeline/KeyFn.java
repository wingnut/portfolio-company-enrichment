package se.wingnut.eqt.pipeline;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import se.wingnut.eqt.domain.Joinable;

/**
 * Generic Apache Beam DoFn for creating an Apache Beam 'keyed' value
 * @param <T>
 */
public class KeyFn<T extends Joinable> extends DoFn<T, KV<String, T>> {
    @ProcessElement
    public void processElement(ProcessContext c) {
        T element = c.element();
        String keyValue = element.getJoinId();
        c.output(KV.of(keyValue, element));
    }
}
