package net.unit8.kysymys.agent;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AgentConfigTest {

    @Test
    void userPropertiesWinOverFileAndDefault(@org.junit.jupiter.api.io.TempDir Path dir)
            throws Exception {
        Files.writeString(dir.resolve("kysymys.properties"),
                "kysymys.url=http://from-file:3000\nkysymys.token=file-token\n");

        AgentConfig cfg = AgentConfig.resolve(
                Map.of("kysymys.token", "cli-token", "kysymys.problemId", "p9"),
                null, dir);

        assertThat(cfg.token()).isEqualTo("cli-token");           // -D beats the file
        assertThat(cfg.url()).isEqualTo("http://from-file:3000"); // file beats the default
        assertThat(cfg.problemId()).isEqualTo("p9");
        assertThat(cfg.isComplete()).isTrue();
    }

    @Test
    void incompleteWhenTokenMissing(@org.junit.jupiter.api.io.TempDir Path dir) {
        AgentConfig cfg = AgentConfig.resolve(Map.of(), Map.of(), dir);
        assertThat(cfg.url()).isEqualTo(AgentConfig.DEFAULT_URL); // default applied
        assertThat(cfg.token()).isNull();
        assertThat(cfg.isComplete()).isFalse();                   // no token => disabled
    }
}
