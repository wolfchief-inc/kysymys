package net.unit8.kysymys.scorer;

import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class KysymysTestLauncher {
    public static void run(Class<?> testClass) {
        String kysymysUrl = System.getProperty("kysymys.url");
        String submissionId = System.getProperty("kysymys.submission.id");
        String token = System.getProperty("kysymys.token");
        boolean standalone = kysymysUrl == null || submissionId == null || token == null;

        Launcher launcher = LauncherFactory.create();
        TestPlan plan = launcher.discover(LauncherDiscoveryRequestBuilder
                .request()
                .selectors(DiscoverySelectors.selectClass(testClass))
                .build());
        KysymysTestExecutionListener testExecutionListener = new KysymysTestExecutionListener();
        launcher.execute(plan, testExecutionListener);

        if (!standalone) {
            HttpClient httpClient = HttpClient.newHttpClient();

            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(kysymysUrl + "/score/" + submissionId + "/" + token))
                        .POST(HttpRequest.BodyPublishers.ofString(""))
                        .headers("content-type", "application/json")
                        .build();
                CompletableFuture<HttpResponse<String>> future = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
                HttpResponse<String> response = future.get(5, TimeUnit.SECONDS);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                throw new RuntimeException("Failure scoring", e);
            }
        }
        System.out.println(testExecutionListener);
    }
}
