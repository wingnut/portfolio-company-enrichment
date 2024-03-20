package se.wingnut.eqt.domain.pc;

import java.io.Serializable;
import java.util.List;

public record PortfolioCompanyData(Slug slug, String website, List<BoardMember> board, String detailedDescription) implements Serializable {
}

