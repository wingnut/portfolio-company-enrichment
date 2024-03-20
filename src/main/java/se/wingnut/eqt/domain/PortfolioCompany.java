package se.wingnut.eqt.domain;

import java.io.Serializable;
import java.util.List;

public record PortfolioCompany (
        String isDivestment,
        String _id,
        String country,
        String entryDate,
        String exitDate,
        List<Fund> fund,
        String path,
        String promotedSdg,
        List<SDG> sdg,
        String sector,
        String title,
        String topic,
        Organization organizationData /* To be added when enriching */) implements Serializable, Joinable {

        @Override
        public String getJoinId() {
                // Unfortunately, there is no proper uuid field for the portfolio company,
                // only the (MongoDB) generated _id which has no meaning in the enrichment json files.
                // So, we are relying on the title field to be unique and correctly spelled...
                return title;
        }
}

record SDG() {} // Not sure what this represents, but it seems to always be an empty array