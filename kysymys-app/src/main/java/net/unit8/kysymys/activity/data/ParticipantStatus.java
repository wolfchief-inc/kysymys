package net.unit8.kysymys.activity.data;

import net.unit8.kysymys.user.data.UserId;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

/**
 * The current work state of one participant, derived from their
 * {@link ActivityEvent} stream for the instructor dashboard.
 *
 * @param participantId  who
 * @param problemId      the problem they were last working on (nullable)
 * @param lastActivityAt the most recent event of any kind — drives idle detection
 * @param lastBuildKind  {@code BUILD_SUCCESS} or {@code BUILD_FAILURE} of the
 *                       latest build event (nullable if they have not built yet)
 * @param lastBuildAt    when that build happened (nullable)
 * @param lastBuildDetail first error line of the latest failing build (nullable)
 * @param stuck          true if their latest {@code STUCK} is newer than their
 *                       latest {@code RESOLVED}
 */
public record ParticipantStatus(
        UserId participantId,
        @Nullable String problemId,
        LocalDateTime lastActivityAt,
        @Nullable ActivityKind lastBuildKind,
        @Nullable LocalDateTime lastBuildAt,
        @Nullable String lastBuildDetail,
        boolean stuck
) {
}
