package se.wingnut.eqt.domain;

import java.io.Serializable;

public record FundData(
        String path,
        String title,
        String preamble,
        String currency,
        String email,
        String heading,
        String indexing,
        String launchDate,
        String size,
        String status) implements Serializable {}
