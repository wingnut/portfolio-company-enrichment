package se.wingnut.eqt.domain;

import se.wingnut.eqt.domain.pc.PortfolioCompanyData;

import java.io.Serializable;
import java.util.List;

public record EnrichedPortfolioCompany(
        // The first fields carry on from the original portfolio company
        String isDivestment,
        String _id,
        String country,
        String entryDate,
        String exitDate,
        String path,
        String promotedSdg,
        List<SDG> sdg,
        String sector,
        String title,
        String topic,
        // To be added when enriching
        Organization organizationData,
        PortfolioCompanyData organisationDetails,
        List<FundData> fundData) implements Serializable {}
