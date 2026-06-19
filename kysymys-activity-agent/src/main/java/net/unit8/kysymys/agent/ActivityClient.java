package net.unit8.kysymys.agent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Best-effort HTTP client that posts a single activity event to
 * {@code {baseUrl}/activity}. Everything here is fire-and-forget: telemetry must
 * never slow down or break a participant's build, so failures are swallowed.
 *
 * <p>Deliberately depends only on the JDK (java.net.http) so the same jar works
 * both inside the Maven process (EventSpy) and in the standalone watcher.
 */
public final class ActivityClient {
    private final String endpoint;
    private final String token;
    private final HttpClient http;

    public ActivityClient(String baseUrl, String token) {
        this.endpoint = stripTrailingSlash(baseUrl) + "/activity";
        this.token = token;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Posts one event. {@code problemId} and {@code detail} may be null.
     * Returns true on a 2xx response, false on any failure (never throws).
     */
    public boolean post(String kind, String problemId, String detail) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(10))
                    .header("content-type", "application/json")
                    .header("x-bouncr-credential", token)
                    .POST(HttpRequest.BodyPublishers.ofString(json(kind, problemId, detail)))
                    .build();
            HttpResponse<Void> res = http.send(request, HttpResponse.BodyHandlers.discarding());
            return res.statusCode() >= 200 && res.statusCode() < 300;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    static String json(String kind, String problemId, String detail) {
        StringBuilder sb = new StringBuilder("{\"kind\":\"").append(kind).append('"');
        if (problemId != null && !problemId.isBlank()) {
            sb.append(",\"problemId\":\"").append(escape(problemId)).append('"');
        }
        if (detail != null && !detail.isBlank()) {
            sb.append(",\"detail\":\"").append(escape(truncate(detail))).append('"');
        }
        return sb.append('}').toString();
    }

    /** The server caps detail at 2000 chars; keep well under that and one line. */
    private static String truncate(String s) {
        String firstLine = s.lines().findFirst().orElse(s).strip();
        return firstLine.length() > 500 ? firstLine.substring(0, 500) : firstLine;
    }

    private static String escape(String s) {
        StringBuilder out = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.toString();
    }

    private static String stripTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
