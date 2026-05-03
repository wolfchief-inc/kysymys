package net.unit8.kysymys.lesson.data;

/**
 * Sealed hierarchy describing where a Problem's source lives. Each subtype
 * knows how to build the public URL of its README.
 */
public sealed interface ProblemRepository
        permits GitHubProblemRepository, BitBucketProblemRepository, GenericProblemRepository {

    String url();

    /** Returns a public URL pointing to the readme/landing page. */
    String problemUrl();
}
