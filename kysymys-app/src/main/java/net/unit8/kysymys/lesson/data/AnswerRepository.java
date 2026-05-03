package net.unit8.kysymys.lesson.data;

public sealed interface AnswerRepository
        permits GitHubAnswerRepository, BitBucketAnswerRepository, GenericAnswerRepository {

    String url();

    /** Returns the public URL for a specific commit on this repository. */
    String commitUrl(CommitHash hash);
}
