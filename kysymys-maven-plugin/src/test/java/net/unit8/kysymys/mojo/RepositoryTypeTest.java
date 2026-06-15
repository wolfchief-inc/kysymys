package net.unit8.kysymys.mojo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RepositoryTypeTest {
    @Test
    void githubHttpsUrl() {
        assertThat(RepositoryType.fromUrl("https://github.com/kawasima/kysymys.git"))
                .isEqualTo(RepositoryType.GITHUB);
    }

    @Test
    void githubSshUrl() {
        assertThat(RepositoryType.fromUrl("git@github.com:kawasima/kysymys.git"))
                .isEqualTo(RepositoryType.GITHUB);
    }

    @Test
    void bitbucketUrl() {
        assertThat(RepositoryType.fromUrl("https://bitbucket.org/team/repo.git"))
                .isEqualTo(RepositoryType.BITBUCKET);
    }

    @Test
    void selfHostedFallsBackToGeneric() {
        assertThat(RepositoryType.fromUrl("https://git.example.com/team/repo.git"))
                .isEqualTo(RepositoryType.GENERIC);
    }

    @Test
    void nullUrlIsGeneric() {
        assertThat(RepositoryType.fromUrl(null)).isEqualTo(RepositoryType.GENERIC);
    }

    @Test
    void wireValuesMatchServerDiscriminator() {
        assertThat(RepositoryType.GITHUB.wireValue()).isEqualTo("github");
        assertThat(RepositoryType.BITBUCKET.wireValue()).isEqualTo("bitbucket");
        assertThat(RepositoryType.GENERIC.wireValue()).isEqualTo("generic");
    }
}
