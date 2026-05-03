package net.unit8.kysymys.user.data;

import java.util.Objects;

/**
 * Stable identifier for a Kysymys user. Other bounded contexts (Lesson,
 * Avatar, Notification) reference users only via this record per ADR 001.
 *
 * <p>The full User aggregate is built in Sub-C; this record is hoisted into
 * Sub-B because Lesson aggregates carry {@code answererId} / {@code commenterId}
 * fields that need a typed identifier today.
 */
public record UserId(String value) {
    public UserId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("UserId value must not be blank");
        }
    }

    public static UserId of(String value) {
        return new UserId(value);
    }
}
