package net.unit8.kysymys.lesson.domain;

import am.ik.yavi.core.ConstraintViolationsException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class RepositoryUrlBuilderTest {
    @Test
    void githubWithoutBranchAndCommitHash() {
        Assertions.assertThatThrownBy(() -> {
            RepositoryUrlBuilder.url("https://github.com/user/repo.git").build();
        }).isInstanceOf(ConstraintViolationsException.class);
    }

    @Test
    void githubWithBranchAndCommitHash() {
        Assertions.assertThatThrownBy(() -> {
            RepositoryUrlBuilder.url("https://github.com/user/repo.git")
                    .branch("branch")
                    .commitHash("0123456789012345678901234567890123456789")
                    .build();
        }).isInstanceOf(ConstraintViolationsException.class);
    }

    @Test
    void githubBranchOnly() {
        Assertions.assertThat(RepositoryUrlBuilder.url("https://github.com/user/repo.git")
                    .branch("branch")
                    .build()).asString().isEqualTo("https://github.com/user/repo/blob/branch");
    }

    @Test
    void githubBranchAndPath() {
        Assertions.assertThat(RepositoryUrlBuilder.url("https://github.com/user/repo.git")
                .branch("branch")
                .path("/a/b")
                .build()).asString().isEqualTo("https://github.com/user/repo/blob/branch/a/b");
    }

    @Test
    void githubBranchViolation() {
        Assertions.assertThatThrownBy(() -> RepositoryUrlBuilder.url("https://github.com/user/repo.git")
                .branch("bra..nch")
                .build()
        ).isInstanceOf(ConstraintViolationsException.class);
        Assertions.assertThatThrownBy(() -> RepositoryUrlBuilder.url("https://github.com/user/repo.git")
                .branch("branch/")
                .build()
        ).isInstanceOf(ConstraintViolationsException.class);
    }

    @Test
    void githubCommitHashOnly() {
        Assertions.assertThat(RepositoryUrlBuilder.url("https://github.com/user/repo.git")
                .commitHash("0123456789012345678901234567890123456789")
                .build()).asString().isEqualTo("https://github.com/user/repo/tree/0123456789012345678901234567890123456789");
    }

    @Test
    void githubCommitHashAndPath() {
        Assertions.assertThat(RepositoryUrlBuilder.url("https://github.com/user/repo.git")
                .commitHash("0123456789012345678901234567890123456789")
                .path("/a/b")
                .build()).asString().isEqualTo("https://github.com/user/repo/tree/0123456789012345678901234567890123456789/a/b");

    }

}