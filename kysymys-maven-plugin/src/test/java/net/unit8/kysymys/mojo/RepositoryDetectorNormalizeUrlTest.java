package net.unit8.kysymys.mojo;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class RepositoryDetectorNormalizeUrlTest {
    @Test
    void httpsRemoteIsNormalized() throws MalformedURLException {
        assertThat(RepositoryDetector.normalizeUrl("https://github.com/kawasima/kysymys.git"))
                .isEqualTo("https://github.com/kawasima/kysymys.git");
    }

    @Test
    void scpLikeSshRemotePassesThroughWithoutThrowing() {
        // new URL(...) would throw MalformedURLException for this form; it must be left intact.
        assertThatCode(() ->
                assertThat(RepositoryDetector.normalizeUrl("git@github.com:kawasima/kysymys.git"))
                        .isEqualTo("git@github.com:kawasima/kysymys.git"))
                .doesNotThrowAnyException();
    }

    @Test
    void sshSchemeRemotePassesThrough() throws MalformedURLException {
        assertThat(RepositoryDetector.normalizeUrl("ssh://git@github.com/kawasima/kysymys.git"))
                .isEqualTo("ssh://git@github.com/kawasima/kysymys.git");
    }
}
