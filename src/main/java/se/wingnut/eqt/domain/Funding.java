package se.wingnut.eqt.domain;

import java.io.Serializable;

public record Funding(
        String uuid,
        String org_uuid,
        String org_name,
        String investor_count,
        String announced_on,
        String investment_type) implements Serializable {
}
