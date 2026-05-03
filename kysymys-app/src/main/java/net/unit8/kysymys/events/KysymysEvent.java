package net.unit8.kysymys.events;

import java.time.LocalDateTime;

/**
 * Marker interface for cross-context domain events. Each subtype is a record
 * that carries the data downstream consumers need; producers (Lesson, User)
 * publish via {@code KysymysEventBus} and consumers (Notification) subscribe
 * by class.
 */
public sealed interface KysymysEvent
        permits SubmittedAnswerEvent, OfferedToFollowEvent, UserCreatedEvent {
    LocalDateTime occurredAt();
}
