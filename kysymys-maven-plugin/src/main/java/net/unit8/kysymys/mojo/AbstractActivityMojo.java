package net.unit8.kysymys.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.Properties;

/**
 * Shared plumbing for the explicit work-state signals a participant can send:
 * {@code mvn kysymys:stuck} and {@code mvn kysymys:resolved}. Both post a single
 * event to {@code {kysymys.url}/activity} authenticated with the same Bouncr JWT
 * used by {@link SubmitMojo}, so the instructor dashboard ties the signal to the
 * right person.
 */
public abstract class AbstractActivityMojo extends AbstractMojo {

    @Parameter(defaultValue = "http://localhost:3000", property = "kysymys.url")
    private String kysymysUrl = "";

    @Parameter(property = "kysymys.problemId")
    private String problemId;

    @Parameter(property = "kysymys.token")
    private String token;

    @Parameter
    private File propertyFile;

    /** The {@code ActivityKind} this goal reports: {@code STUCK} or {@code RESOLVED}. */
    protected abstract String kind();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (propertyFile != null) {
            Properties props = new Properties();
            try (Reader reader = new FileReader(propertyFile)) {
                props.load(reader);
                problemId = Objects.requireNonNullElse(props.getProperty("kysymys.problemId"), problemId);
                kysymysUrl = Objects.requireNonNullElse(props.getProperty("kysymys.url"), kysymysUrl);
                token = Objects.requireNonNullElse(props.getProperty("kysymys.token"), token);
            } catch (IOException e) {
                throw new MojoExecutionException("Failure to read properties from the file", e);
            }
        }

        if (isBlank(kysymysUrl)) {
            throw new MojoExecutionException("kysymys.url must be set");
        }
        if (isBlank(token)) {
            throw new MojoExecutionException(
                    "kysymys.token (Bouncr credential) must be set, e.g. -Dkysymys.token=<JWT>");
        }

        String payload = "{\"kind\":\"" + kind() + "\""
                + (isBlank(problemId) ? "" : ",\"problemId\":\"" + escape(problemId) + "\"")
                + "}";

        URI endpoint = URI.create(stripTrailingSlash(kysymysUrl) + "/activity");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(endpoint)
                .timeout(Duration.ofSeconds(30))
                .header("content-type", "application/json")
                .header("x-bouncr-credential", token)
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        getLog().info("Reporting " + kind() + " to " + endpoint);
        HttpResponse<String> response;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new MojoExecutionException("Failure to report " + kind(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MojoExecutionException("Interrupted while reporting " + kind(), e);
        }

        int status = response.statusCode();
        if (status >= 200 && status < 300) {
            getLog().info(kind() + " reported (status " + status + ")");
        } else {
            getLog().error(response.body());
            throw new MojoFailureException(kind() + " rejected with status " + status);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String stripTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
