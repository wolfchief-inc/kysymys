package net.unit8.kysymys.lesson.data;

import java.time.LocalDateTime;

public sealed interface ProblemEvent
        permits ProblemCreatedEvent, ProblemUpdatedEvent, ProblemArchivedEvent {

    ProblemEventId id();
    ProblemLifecycleId lifecycleId();
    LocalDateTime occurredAt();
}
