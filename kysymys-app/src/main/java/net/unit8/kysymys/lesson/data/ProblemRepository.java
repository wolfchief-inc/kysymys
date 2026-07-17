package net.unit8.kysymys.lesson.data;

import org.jspecify.annotations.Nullable;

/**
 * Sealed hierarchy describing where a Problem's source lives. Each subtype
 * knows how to build the public URL of its README.
 */
public sealed interface ProblemRepository
        permits GitHubProblemRepository, BitBucketProblemRepository, GenericProblemRepository {

    String url();

    /** The branch that holds the problem's source. */
    String branch();

    /** Path to the readme/landing file within the branch, or {@code null} when the provider has none. */
    @Nullable String readmePath();

    /** Discriminator key persisted in the {@code runner} column and emitted as the JSON {@code type} tag. */
    String typeKey();

    /** Returns a public URL pointing to the readme/landing page. */
    String problemUrl();
}
