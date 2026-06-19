package net.unit8.kysymys.agent;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/**
 * Resolves the agent's settings ({@code kysymys.url}, {@code kysymys.token},
 * {@code kysymys.problemId}) the same way {@code SubmitMojo} does, so a
 * participant configures things once.
 *
 * <p>Precedence: explicit Maven {@code -D} / user properties, then system
 * properties, then a {@code kysymys.properties} file in the project's working
 * directory. If {@code url} or {@code token} cannot be resolved the agent stays
 * a no-op — telemetry never blocks a build.
 */
public record AgentConfig(String url, String token, String problemId, Path workingDir) {

    public static final String DEFAULT_URL = "http://localhost:3000";

    public boolean isComplete() {
        return url != null && !url.isBlank() && token != null && !token.isBlank();
    }

    /**
     * @param userProperties Maven user properties ({@code -Dkey=value}); may be null
     * @param systemProperties JVM system properties; may be null
     * @param workingDir the project working directory
     */
    public static AgentConfig resolve(Map<String, String> userProperties,
                                      Map<String, String> systemProperties,
                                      Path workingDir) {
        Properties file = readPropertiesFile(workingDir);

        String url = firstNonBlank(
                get(userProperties, "kysymys.url"),
                get(systemProperties, "kysymys.url"),
                file.getProperty("kysymys.url"),
                DEFAULT_URL);
        String token = firstNonBlank(
                get(userProperties, "kysymys.token"),
                get(systemProperties, "kysymys.token"),
                file.getProperty("kysymys.token"),
                System.getenv("KYSYMYS_TOKEN"));
        String problemId = firstNonBlank(
                get(userProperties, "kysymys.problemId"),
                get(systemProperties, "kysymys.problemId"),
                file.getProperty("kysymys.problemId"));

        return new AgentConfig(url, token, problemId, workingDir);
    }

    private static Properties readPropertiesFile(Path workingDir) {
        Properties props = new Properties();
        if (workingDir == null) {
            return props;
        }
        Path file = workingDir.resolve("kysymys.properties");
        if (Files.isRegularFile(file)) {
            try (Reader r = Files.newBufferedReader(file)) {
                props.load(r);
            } catch (IOException ignored) {
                // best-effort: a missing/unreadable file just means no overrides
            }
        }
        return props;
    }

    private static String get(Map<String, String> m, String key) {
        return m == null ? null : m.get(key);
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }
}
