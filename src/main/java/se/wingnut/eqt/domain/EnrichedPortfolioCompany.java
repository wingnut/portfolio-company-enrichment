package se.wingnut.eqt.domain;

import java.io.Serializable;
import java.util.List;

public record EnrichedPortfolioCompany(
        // The first fields carry on from the original portfolio company
        String _id,
        String country,
        String entryDate,
        List<Fund> fund,
        String path,
        String promotedSdg,
        List<SDG> sdg,
        String sector,
        String title,
        String topic,
        // To be added when enriching
        Organization organizationData) implements Serializable {}
