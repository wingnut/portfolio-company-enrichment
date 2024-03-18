package se.wingnut.eqt.pipeline;

import com.google.gson.Gson;
import org.apache.beam.sdk.values.KV;
import org.junit.jupiter.api.Test;
import se.wingnut.eqt.domain.Organization;
import se.wingnut.eqt.domain.PortfolioCompany;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PortfolioCompanyToJsonFnTest {
    PortfolioCompany portfolioCompany = new PortfolioCompany(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "My Company",
            null,
            null
    );
    Organization organization = new Organization(
            "some-long-uuid",
            "My Company",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
    );

    PortfolioCompany expectedEnrichedPortfolioCompany = new PortfolioCompany(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "My Company",
            null,
            new Organization(
                    "some-long-uuid",
                    "My Company",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null)
    );

    String expectedJson = new Gson().toJson(expectedEnrichedPortfolioCompany);

    @Test
    void processElement_Organization() {
        String json = new PortfolioCompanyToJsonFn().apply(KV.of("My Company", KV.of(portfolioCompany, organization)));
        assertEquals(expectedJson, json);
    }

}
