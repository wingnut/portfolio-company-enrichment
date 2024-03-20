package se.wingnut.eqt.http.fund;

import se.wingnut.eqt.domain.FundData;

import java.io.Serializable;

public record Data(FundData sanityFund) implements Serializable {}
