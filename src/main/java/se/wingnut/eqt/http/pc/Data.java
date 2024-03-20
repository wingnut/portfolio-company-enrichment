package se.wingnut.eqt.http.pc;


import se.wingnut.eqt.domain.pc.PortfolioCompanyDataRaw;

import java.io.Serializable;

public record Data(PortfolioCompanyDataRaw sanityCompanyPage) implements Serializable {}
