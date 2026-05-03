package net.unit8.kysymys.lesson.data;

import java.time.LocalDateTime;
import java.util.Objects;

public record Submission(
        SubmissionId id,
        AnswerId answerId,
        CommitHash commitHash,
        LocalDateTime submittedAt
) {
    public Submission {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(answerId, "answerId");
        Objects.requireNonNull(commitHash, "commitHash");
        Objects.requireNonNull(submittedAt, "submittedAt");
    }
}
