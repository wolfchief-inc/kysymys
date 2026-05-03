package net.unit8.kysymys.lesson.data;

import net.unit8.kysymys.user.data.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

public record ProblemArchivedEvent(
        ProblemEventId id,
        ProblemLifecycleId lifecycleId,
        LocalDateTime occurredAt,
        UserId archiverId
) implements ProblemEvent {
    public ProblemArchivedEvent {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(lifecycleId, "lifecycleId");
        Objects.requireNonNull(occurredAt, "occurredAt");
        Objects.requireNonNull(archiverId, "archiverId");
    }
}
