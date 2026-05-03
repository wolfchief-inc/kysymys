package net.unit8.kysymys.events;

import net.unit8.kysymys.user.data.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

public record UserCreatedEvent(UserId userId, LocalDateTime occurredAt) implements KysymysEvent {
    public UserCreatedEvent {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(occurredAt, "occurredAt");
    }
}
