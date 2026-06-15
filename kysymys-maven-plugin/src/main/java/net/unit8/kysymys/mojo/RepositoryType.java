package net.unit8.kysymys.mojo;

import java.util.Locale;

/**
 * The repository hosting providers the kysymys server understands as the {@code "type"}
 * discriminator of an answer repository payload.
 */
public enum RepositoryType {
    GITHUB("github"),
    BITBUCKET("bitbucket"),
    GENERIC("generic");

    private final String wireValue;

    RepositoryType(String wireValue) {
        this.wireValue = wireValue;
    }

    /** The value sent as the {@code "type"} field. */
    public String wireValue() {
        return wireValue;
    }

    /**
     * Infers the hosting provider from a remote URL's host. Anything that is neither GitHub nor
     * Bitbucket is reported as {@link #GENERIC}.
     */
    public static RepositoryType fromUrl(String url) {
        String host = host(url);
        if (matchesHost(host, "github.com")) {
            return GITHUB;
        }
        if (matchesHost(host, "bitbucket.org")) {
            return BITBUCKET;
        }
        return GENERIC;
    }

    /**
     * Extracts the host from a git remote URL. Handles scheme URLs
     * ({@code https://host/...}, {@code ssh://git@host:22/...}) and SCP-like SSH remotes
     * ({@code git@host:owner/repo.git}). Returns an empty string when no host can be determined.
     */
    static String host(String url) {
        if (url == null) {
            return "";
        }
        String s = url.trim();
        int scheme = s.indexOf("://");
        if (scheme >= 0) {
            s = s.substring(scheme + 3);
        }
        int at = s.indexOf('@');
        if (at >= 0) {
            s = s.substring(at + 1);
        }
        // The host ends at the first '/', ':' (port or SCP path separator).
        int end = s.length();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '/' || c == ':') {
                end = i;
                break;
            }
        }
        return s.substring(0, end).toLowerCase(Locale.ROOT);
    }

    private static boolean matchesHost(String host, String domain) {
        return host.equals(domain) || host.endsWith("." + domain);
    }
}
