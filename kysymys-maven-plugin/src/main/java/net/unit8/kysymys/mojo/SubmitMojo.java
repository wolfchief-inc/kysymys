package net.unit8.kysymys.mojo;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Submit an answer.
 *
 */
@Mojo(name = "submit")
public class SubmitMojo extends AbstractMojo {
    /**
     * The URL of a kysysmys server.
     */
    @Parameter(defaultValue = "http://localhost:8080", property = "kysymys.url")
    private String kysymysUrl = "";

    /**
     * The problem ID.
     */
    @Parameter(property = "kysymys.problemId")
    private String problemId;

    @Parameter
    private File propertyFile;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (propertyFile != null) {
            Properties props = new Properties();
            try(Reader reader = new FileReader(propertyFile)) {
                props.load(reader);
                problemId = Objects.requireNonNullElse(props.getProperty("kysymys.problemId"), problemId);
                kysymysUrl = Objects.requireNonNullElse(props.getProperty("kysymys.url"), kysymysUrl);
            } catch (IOException e) {
                throw new MojoExecutionException("Failure to read properties from the file", e);
            }
        }

        if (problemId == null || kysymysUrl == null) {
            throw new MojoExecutionException("Must be set the url and problemId");
        }
        RepositoryDetector repositoryDetector = new RepositoryDetector();
        RepositoryDetectionResult result;
        try {
            result = repositoryDetector.detect();
        } catch (IOException e) {
            throw new MojoExecutionException("", e);
        }
        String token = NanoIdUtils.randomNanoId();
        ObjectMapper mapper = new ObjectMapper();
        AnswerRepositoryDto answerRepository = new AnswerRepositoryDto(problemId,
                result.getRepositoryUrl(),
                result.getCommitHash());

        HttpClient httpClient = HttpClient.newHttpClient();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(kysymysUrl + "/token/watch/" + token))
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(answerRepository), StandardCharsets.UTF_8))
                    .headers("content-type", "application/json")
                    .build();
            getLog().info("url:" + URI.create(kysymysUrl + "/token/watch/" + token));
            CompletableFuture<HttpResponse<String>> future = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

            Desktop desktop = Desktop.getDesktop();
            desktop.browse(URI.create(kysymysUrl + "/token/publish/" + token));

            HttpResponse<String> response = future.get(30, TimeUnit.SECONDS);
            getLog().info("status:" + response.statusCode());
            getLog().info(response.body());
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new MojoExecutionException("", e);
        } catch (TimeoutException e) {
            throw new MojoExecutionException("Time out", e);
        }
    }
}
