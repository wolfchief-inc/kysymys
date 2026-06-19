package net.unit8.kysymys.activity.resource;

import enkan.Endpoint;
import enkan.web.data.HttpRequest;
import enkan.web.data.HttpResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Serves the {@code kysymys-agent} Go binary so the participant-side Maven
 * extension can download the one matching its host on first build — no manual
 * install, no binaries bundled in the extension jar.
 *
 * <p>{@code GET /agent/<os-arch>} streams the binary as
 * {@code application/octet-stream}. Mounted as an {@link Endpoint} (not a
 * resource) so it sits before content negotiation and authentication: the
 * binaries are not secret and a participant must be able to fetch one before any
 * token round-trip. The per-platform binaries are cross-compiled into
 * {@code agent-bin/<os-arch>/} at build time (see kysymys-app/pom.xml).
 */
public class AgentBinaryEndpoint implements Endpoint<HttpRequest, HttpResponse> {

    public static final Pattern PATH =
            Pattern.compile("^/agent/(darwin-arm64|windows-amd64|linux-amd64)/?$");

    @Override
    public HttpResponse handle(HttpRequest request) {
        Matcher m = PATH.matcher(request.getUri());
        if (!m.matches()) {
            return notFound();
        }
        String tag = m.group(1);
        String exe = tag.startsWith("windows") ? "kysymys-agent.exe" : "kysymys-agent";
        String resource = "agent-bin/" + tag + "/" + exe;

        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                return notFound(); // binary not built into this server
            }
            byte[] bytes = in.readAllBytes();
            HttpResponse response = HttpResponse.of(new ByteArrayInputStream(bytes));
            response.getHeaders().put("Content-Type", "application/octet-stream");
            response.getHeaders().put("Content-Length", String.valueOf(bytes.length));
            response.getHeaders().put("Content-Disposition", "attachment; filename=\"" + exe + "\"");
            response.setStatus(200);
            return response;
        } catch (Exception e) {
            HttpResponse r = HttpResponse.of("Internal Server Error");
            r.setStatus(500);
            return r;
        }
    }

    private static HttpResponse notFound() {
        HttpResponse r = HttpResponse.of("Not Found");
        r.setStatus(404);
        return r;
    }
}
