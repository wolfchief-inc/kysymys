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
        String lower = url == null ? "" : url.toLowerCase(Locale.ROOT);
        if (lower.contains("github.com")) {
            return GITHUB;
        }
        if (lower.contains("bitbucket.org")) {
            return BITBUCKET;
        }
        return GENERIC;
    }
}
