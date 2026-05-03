package net.unit8.kysymys.lesson.data;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProblemRepositoryUrlTest {
    @Test
    void githubProblemUrlBuildsBlobLink() {
        ProblemRepository repo = new GitHubProblemRepository(
                "https://github.com/foo/bar.git", "main", "/README.md");
        assertThat(repo.problemUrl())
                .isEqualTo("https://github.com/foo/bar/blob/main/README.md");
    }

    @Test
    void githubProblemUrlAllowsNoDotGitSuffix() {
        ProblemRepository repo = new GitHubProblemRepository(
                "https://github.com/foo/bar", "main", "/README.md");
        assertThat(repo.problemUrl())
                .isEqualTo("https://github.com/foo/bar/blob/main/README.md");
    }

    @Test
    void bitbucketProblemUrlBuildsSrcLink() {
        ProblemRepository repo = new BitBucketProblemRepository(
                "https://bitbucket.org/foo/bar", "main", "/README.md");
        assertThat(repo.problemUrl())
                .isEqualTo("https://bitbucket.org/foo/bar/src/main/README.md");
    }

    @Test
    void genericProblemUrlReturnsTheBaseUrl() {
        ProblemRepository repo = new GenericProblemRepository(
                "https://example.com/lessons/kafka", "main");
        assertThat(repo.problemUrl())
                .isEqualTo("https://example.com/lessons/kafka");
    }

    @Test
    void rejectsBlankUrl() {
        assertThatThrownBy(() ->
                new GitHubProblemRepository("", "main", "/README.md"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBranchWithDoubleDots() {
        assertThatThrownBy(() ->
                new GitHubProblemRepository("https://github.com/x/y", "ma..in", "/README.md"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBranchEndingWithSlash() {
        assertThatThrownBy(() ->
                new GitHubProblemRepository("https://github.com/x/y", "main/", "/README.md"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
