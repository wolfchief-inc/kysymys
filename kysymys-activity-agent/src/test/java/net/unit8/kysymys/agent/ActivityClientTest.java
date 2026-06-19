package net.unit8.kysymys.agent;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityClientTest {

    @Test
    void omitsOptionalFieldsWhenAbsent() {
        assertThat(ActivityClient.json("BUILD_SUCCESS", null, null))
                .isEqualTo("{\"kind\":\"BUILD_SUCCESS\"}");
    }

    @Test
    void includesProblemIdAndDetail() {
        assertThat(ActivityClient.json("BUILD_FAILURE", "p1", "compile error"))
                .isEqualTo("{\"kind\":\"BUILD_FAILURE\",\"problemId\":\"p1\",\"detail\":\"compile error\"}");
    }

    @Test
    void escapesQuotesAndCollapsesToFirstLine() {
        String json = ActivityClient.json("BUILD_FAILURE", null,
                "error: \"x\" expected\nstack line two\nstack line three");
        assertThat(json).isEqualTo(
                "{\"kind\":\"BUILD_FAILURE\",\"detail\":\"error: \\\"x\\\" expected\"}");
    }
}
