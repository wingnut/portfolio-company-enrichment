package se.wingnut.eqt.domain;

import java.io.Serializable;

public record Organization (
        String uuid,
        String name,
        String homepage_url,
        String country_code,
        String city,
        String founded_on,
        String short_description,
        String employee_count,
        String num_funding_rounds,
        String last_funding_on,
        String total_funding_usd) implements Serializable, Joinable {
    @Override
    public String getJoinId() {
        // Unfortunately, there is no proper uuid field for the portfolio company,
        // only the (MongoDB) generated _id which has no meaning in the enrichment json files.
        // So, we are relying on the name field to be unique and correctly spelled AND to match the title field in PortfolioCompany...
        return name;
    }
}
