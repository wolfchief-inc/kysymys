package net.unit8.kysymys.lesson.data;

import java.util.Objects;

public record Problem(
        ProblemId id,
        ProblemName name,
        ProblemRepository repository,
        ProblemLifecycleId lifecycleId
) {
    public Problem {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(repository, "repository");
        Objects.requireNonNull(lifecycleId, "lifecycleId");
    }
}
