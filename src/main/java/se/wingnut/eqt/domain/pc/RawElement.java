package se.wingnut.eqt.domain.pc;

import java.io.Serializable;
import java.util.List;

public record RawElement(List<Child> children) implements Serializable {
}
