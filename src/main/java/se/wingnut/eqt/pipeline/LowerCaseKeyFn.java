package se.wingnut.eqt.pipeline;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import se.wingnut.eqt.domain.Joinable;

/**
 * Generic Apache Beam DoFn for creating an Apache Beam 'keyed' value.
 * The variation here is that the key is "lower cased" in hope of getting more matches
 */
public class LowerCaseKeyFn<T extends Joinable> extends DoFn<T, KV<String, T>> {
    @ProcessElement
    public void processElement(ProcessContext c) {
        T element = c.element();
        String keyValue = element.getJoinId().toLowerCase();
        c.output(KV.of(keyValue, element));
    }
}
