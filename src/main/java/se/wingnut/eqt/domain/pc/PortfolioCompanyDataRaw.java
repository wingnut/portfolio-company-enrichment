package se.wingnut.eqt.domain.pc;

import java.io.Serializable;
import java.util.List;

public record PortfolioCompanyDataRaw(Slug slug, String website, List<BoardMember> board, List<RawElement> _rawBody) implements Serializable {
}

