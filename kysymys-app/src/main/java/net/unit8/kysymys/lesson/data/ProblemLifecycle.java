package net.unit8.kysymys.lesson.data;

import java.util.Objects;

public record ProblemLifecycle(
        ProblemLifecycleId id,
        ProblemId problemId,
        ProblemStatus status
) {
    public ProblemLifecycle {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(problemId, "problemId");
        Objects.requireNonNull(status, "status");
    }
}
