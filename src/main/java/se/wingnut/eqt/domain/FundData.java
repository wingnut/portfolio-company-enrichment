package se.wingnut.eqt.domain;

import java.io.Serializable;

public record FundData(String path, String title, String preamble) implements Serializable {
}
