package net.unit8.kysymys.lesson.data;

import net.unit8.kysymys.user.data.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

public record ProblemCreatedEvent(
        ProblemEventId id,
        ProblemLifecycleId lifecycleId,
        LocalDateTime occurredAt,
        UserId creatorId
) implements ProblemEvent {
    public ProblemCreatedEvent {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(lifecycleId, "lifecycleId");
        Objects.requireNonNull(occurredAt, "occurredAt");
        Objects.requireNonNull(creatorId, "creatorId");
    }
}
