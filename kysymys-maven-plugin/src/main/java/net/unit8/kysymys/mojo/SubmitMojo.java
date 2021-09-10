package net.unit8.kysymys.mojo;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;


@Mojo(name = "submit")
public class SubmitMojo extends AbstractMojo {
    @Parameter(defaultValue = "http://localhost:8080")
    private String kysymysUrl;

    @Parameter
    private String problemId;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        RepositoryDetector repositoryDetector = new RepositoryDetector();
        RepositoryDetectionResult result;
        try {
            result = repositoryDetector.detect();
        } catch (IOException e) {
            throw new MojoExecutionException("", e);
        }
        String token = NanoIdUtils.randomNanoId();
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.browse(URI.create(kysymysUrl + "/token/publish/" + token));
        } catch (IOException e) {
            throw new MojoExecutionException("", e);
        }
        ObjectMapper mapper = new ObjectMapper();
        AnswerRepositoryDto answerRepository = new AnswerRepositoryDto(problemId,
                result.getRepositoryUrl(),
                result.getCommitHash());

        HttpClient httpClient = HttpClient.newHttpClient();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(kysymysUrl))
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(answerRepository), StandardCharsets.UTF_8))
                    .build();
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new MojoExecutionException("", e);
        }
    }
}
