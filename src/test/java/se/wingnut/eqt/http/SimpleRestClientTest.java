package se.wingnut.eqt.http;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class SimpleRestClientTest {

    @Disabled("We don't really need to test the functionality of okhttp3 and Gson, but while creating a wrapper for it it helps to run a manual sanity check test every now and then")
    @Test
    void get() {
        System.out.println(new SimpleRestClient().get("https://eqtgroup.com/page-data/current-portfolio/funds/eqt-growth/page-data.json", FundResponse.class));
    }

    record FundResponse(Result result) {}
    record Result(Data data) {}
    record Data(SanityFund sanityFund) {}
    record SanityFund(String title, String preamble) {}

}
