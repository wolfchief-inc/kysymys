package net.unit8.kysymys.lesson.data;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnswerRepositoryUrlTest {
    @Test
    void githubAnswerUrlForCommit() {
        AnswerRepository repo = new GitHubAnswerRepository("https://github.com/me/fizzbuzz.git");
        String hash = "0123456789012345678901234567890123456789";
        assertThat(repo.commitUrl(new CommitHash(hash)))
                .isEqualTo("https://github.com/me/fizzbuzz/tree/" + hash);
    }

    @Test
    void bitbucketAnswerUrlForCommit() {
        AnswerRepository repo = new BitBucketAnswerRepository("https://bitbucket.org/me/fizzbuzz");
        String hash = "0123456789012345678901234567890123456789";
        assertThat(repo.commitUrl(new CommitHash(hash)))
                .isEqualTo("https://bitbucket.org/me/fizzbuzz/commits/" + hash);
    }

    @Test
    void genericAnswerUrlReturnsTheBaseUrl() {
        AnswerRepository repo = new GenericAnswerRepository("https://example.com/u/me");
        String hash = "0123456789012345678901234567890123456789";
        assertThat(repo.commitUrl(new CommitHash(hash)))
                .isEqualTo("https://example.com/u/me");
    }
}
