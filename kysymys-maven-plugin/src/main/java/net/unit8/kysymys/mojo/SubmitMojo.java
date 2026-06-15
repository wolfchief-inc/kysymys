package net.unit8.kysymys.mojo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
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
 * Submits the current working copy as an answer to a kysymys problem.
 *
 * <p>Posts {@code {"repository": {"type", "url"}, "commitHash"}} to
 * {@code POST {kysymys.url}/problems/{kysymys.problemId}/answers}, authenticating with the Bouncr
 * JWT supplied via {@code kysymys.token} in the {@code x-bouncr-credential} header.
 */
@Mojo(name = "submit")
public class SubmitMojo extends AbstractMojo {
    /**
     * The base URL of the kysymys server.
     */
    @Parameter(defaultValue = "http://localhost:3000", property = "kysymys.url")
    private String kysymysUrl = "";

    /**
     * The problem ID to answer.
     */
    @Parameter(property = "kysymys.problemId")
    private String problemId;

    /**
     * The Bouncr credential (JWT) sent as {@code x-bouncr-credential}. Configure it on the command
     * line ({@code -Dkysymys.token=...}), in {@code settings.xml}, or in {@link #propertyFile}.
     */
    @Parameter(property = "kysymys.token")
    private String token;

    @Parameter
    private File propertyFile;

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

        if (isBlank(kysymysUrl) || isBlank(problemId)) {
            throw new MojoExecutionException("Both kysymys.url and kysymys.problemId must be set");
        }
        if (isBlank(token)) {
            throw new MojoExecutionException(
                    "kysymys.token (Bouncr credential) must be set, e.g. -Dkysymys.token=<JWT>");
        }

        RepositoryDetectionResult detection;
        try {
            detection = new RepositoryDetector().detect();
        } catch (IOException e) {
            throw new MojoExecutionException("Failure to detect the git repository", e);
        }

        String repositoryUrl = detection.getRepositoryUrl();
        SubmitAnswerRequest body = new SubmitAnswerRequest(
                new AnswerRepositoryDto(RepositoryType.fromUrl(repositoryUrl).wireValue(), repositoryUrl),
                detection.getCommitHash());

        ObjectMapper mapper = new ObjectMapper();
        String payload;
        try {
            payload = mapper.writeValueAsString(body);
        } catch (IOException e) {
            throw new MojoExecutionException("Failure to serialize the request body", e);
        }

        URI endpoint = URI.create(stripTrailingSlash(kysymysUrl) + "/problems/" + problemId + "/answers");
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(endpoint)
                .timeout(Duration.ofSeconds(30))
                .header("content-type", "application/json")
                .header("x-bouncr-credential", token)
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        getLog().info("Submitting answer to " + endpoint);
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new MojoExecutionException("Failure to submit the answer", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MojoExecutionException("Interrupted while submitting the answer", e);
        }

        int status = response.statusCode();
        if (status >= 200 && status < 300) {
            getLog().info("Answer submitted (status " + status + ")");
            getLog().info(response.body());
        } else {
            getLog().error(response.body());
            throw new MojoFailureException("Submission rejected with status " + status);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String stripTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
