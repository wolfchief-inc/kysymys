package net.unit8.kysymys.agent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

/**
 * Obtains the {@code kysymys-agent} Go binary for the host OS/arch by downloading
 * it from the kysymys server ({@code GET {baseUrl}/agent/<os-arch>}) on first use
 * and caching it under {@code ~/.kysymys/bin/}. Nothing is bundled in this jar and
 * the participant installs nothing by hand.
 *
 * <p>Returns {@code null} for any platform the server doesn't ship a binary for,
 * or if the download fails — the extension then simply skips the heartbeat
 * watcher (build-outcome reporting over HTTP is unaffected).
 */
public final class AgentBinary {
    private AgentBinary() {}

    /**
     * @param baseUrl the kysymys server base URL (same as {@code kysymys.url})
     * @return the cached executable path, or {@code null} if unavailable
     */
    public static Path ensureDownloaded(String baseUrl) {
        String tag = osArchTag(System.getProperty("os.name"), System.getProperty("os.arch"));
        if (tag == null || baseUrl == null || baseUrl.isBlank()) {
            return null;
        }
        boolean windows = tag.startsWith("windows");
        String exe = windows ? "kysymys-agent.exe" : "kysymys-agent";
        Path dest = Path.of(System.getProperty("user.home"), ".kysymys", "bin", exe);

        try {
            if (Files.isRegularFile(dest) && Files.size(dest) > 0) {
                return dest; // already downloaded; delete the file to force a refresh
            }
            Files.createDirectories(dest.getParent());

            URI uri = URI.create(stripTrailingSlash(baseUrl) + "/agent/" + tag);
            HttpClient http = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            HttpRequest req = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            Path tmp = dest.resolveSibling(exe + ".tmp");
            HttpResponse<Path> res = http.send(req,
                    HttpResponse.BodyHandlers.ofFile(tmp,
                            java.nio.file.StandardOpenOption.CREATE,
                            java.nio.file.StandardOpenOption.TRUNCATE_EXISTING,
                            java.nio.file.StandardOpenOption.WRITE));
            if (res.statusCode() != 200 || Files.size(tmp) == 0) {
                Files.deleteIfExists(tmp);
                return null;
            }
            Files.move(tmp, dest, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            if (!windows) {
                dest.toFile().setExecutable(true, false);
            }
            return dest;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Maps JVM {@code os.name}/{@code os.arch} to one of the served binary tags
     * ({@code darwin-arm64}, {@code windows-amd64}, {@code linux-amd64}), or
     * {@code null} if the server doesn't ship that combination.
     */
    static String osArchTag(String osName, String osArch) {
        if (osName == null || osArch == null) {
            return null;
        }
        String n = osName.toLowerCase();
        String a = osArch.toLowerCase();
        String os;
        if (n.contains("mac") || n.contains("darwin")) {
            os = "darwin";
        } else if (n.contains("win")) {
            os = "windows";
        } else if (n.contains("linux")) {
            os = "linux";
        } else {
            return null;
        }
        String arch;
        if (a.equals("aarch64") || a.equals("arm64")) {
            arch = "arm64";
        } else if (a.equals("amd64") || a.equals("x86_64")) {
            arch = "amd64";
        } else {
            return null;
        }
        String tag = os + "-" + arch;
        return switch (tag) {
            case "darwin-arm64", "windows-amd64", "linux-amd64" -> tag;
            default -> null; // e.g. darwin-amd64 / linux-arm64 not served
        };
    }

    private static String stripTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
