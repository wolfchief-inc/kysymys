package net.unit8.kysymys.activity.data;

import net.unit8.kysymys.user.data.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A single work-activity telemetry record reported by a participant's machine.
 *
 * <p>{@code participantId} is the Bouncr principal of the submitter (per ADR 001
 * other contexts reference users only by {@link UserId}). {@code problemId} and
 * {@code detail} are optional: a heartbeat carries neither, a build failure
 * carries the first error line in {@code detail}.
 */
public record ActivityEvent(
        ActivityEventId id,
        UserId participantId,
        String problemId,
        ActivityKind kind,
        String detail,
        LocalDateTime occurredAt
) {
    public ActivityEvent {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(participantId, "participantId");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(occurredAt, "occurredAt");
    }
}
