package se.wingnut.eqt.http;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import se.wingnut.eqt.domain.pc.PortfolioCompanyDataRaw;
import se.wingnut.eqt.http.fund.FundResponse;
import se.wingnut.eqt.http.pc.PortfolioCompanyResponse;

import java.util.List;
import java.util.stream.Collectors;

public class SimpleRestClientTest {

    @Disabled("We don't really need to test the functionality of okhttp3 and Gson, but while creating a wrapper for it it helps to run a manual sanity check test every now and then")
    @Test
    void getFund() {
        System.out.println(new SimpleRestClient().get("https://eqtgroup.com/page-data/current-portfolio/funds/eqt-growth/page-data.json", FundResponse.class));
    }

    @Disabled("We don't really need to test the functionality of okhttp3 and Gson, but while creating a wrapper for it it helps to run a manual sanity check test every now and then")
    @Test
    void getPC() {
        PortfolioCompanyResponse pcr = new SimpleRestClient().get("https://eqtgroup.com/page-data/current-portfolio/anticimex-future/page-data.json", PortfolioCompanyResponse.class);
        PortfolioCompanyDataRaw pc = pcr.result().data().sanityCompanyPage();
        System.out.println(pc.slug() + " " + pc.website() + " " + pc.board());
        List<String> childTexts = pc._rawBody().stream()
                .map(re -> re.children()) // We're only interested in the text field down in the child elements
                .flatMap(List::stream)
                .filter(child -> child._type().equals("span")) // We're only interested child elements of type span (that typically contains text)
                .map(child -> child.text().replaceAll("\n", " ")) // Remove any line breaks
                .collect(Collectors.toList());
        System.out.println(String.join(" ", childTexts));
    }

}
