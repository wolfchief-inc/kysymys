package net.unit8.kysymys.activity.dao;

import net.unit8.kysymys.activity.data.ActivityEvent;
import net.unit8.kysymys.activity.data.ActivityKind;
import net.unit8.kysymys.activity.data.ParticipantStatus;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

/**
 * Persists {@code activity_events} and derives the per-participant
 * {@link ParticipantStatus} projection the dashboard reads.
 */
public class ActivityEventDao {
    private static final Field<String> ID = field("id", String.class);
    private static final Field<String> PARTICIPANT_ID = field("participant_id", String.class);
    private static final Field<String> PROBLEM_ID = field("problem_id", String.class);
    private static final Field<String> KIND = field("kind", String.class);
    private static final Field<String> DETAIL = field("detail", String.class);
    private static final Field<LocalDateTime> OCCURRED_AT = field("occurred_at", LocalDateTime.class);

    private final DSLContext dsl;

    public ActivityEventDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void insert(ActivityEvent e) {
        dsl.insertInto(table("activity_events"), ID, PARTICIPANT_ID, PROBLEM_ID, KIND, DETAIL, OCCURRED_AT)
                .values(e.id().value(), e.participantId().value(), e.problemId(),
                        e.kind().name(), e.detail(), e.occurredAt())
                .execute();
    }

    /**
     * Derives the current status of every participant who has reported at least
     * one event. Training-scale: a single session's event volume is small, so we
     * fetch the events ordered by time and reduce in Java rather than reaching for
     * window functions in the hand-written DSL. Revisit if event volume grows.
     */
    public List<ParticipantStatus> listStatuses() {
        Map<String, Acc> accs = new LinkedHashMap<>();
        for (Record r : dsl.select(PARTICIPANT_ID, PROBLEM_ID, KIND, DETAIL, OCCURRED_AT)
                .from(table("activity_events"))
                .orderBy(OCCURRED_AT.asc())
                .fetch()) {
            String pid = r.get(PARTICIPANT_ID);
            ActivityKind kind = ActivityKind.valueOf(r.get(KIND));
            LocalDateTime at = r.get(OCCURRED_AT);
            Acc a = accs.computeIfAbsent(pid, k -> new Acc());
            a.lastActivityAt = at; // events are ascending, so the last seen is the max
            if (r.get(PROBLEM_ID) != null) {
                a.problemId = r.get(PROBLEM_ID);
            }
            switch (kind) {
                case BUILD_SUCCESS, BUILD_FAILURE -> {
                    a.lastBuildKind = kind;
                    a.lastBuildAt = at;
                    a.lastBuildDetail = r.get(DETAIL);
                }
                case STUCK -> a.stuckAt = at;
                case RESOLVED -> a.resolvedAt = at;
                case HEARTBEAT -> { /* heartbeat only advances lastActivityAt */ }
            }
        }

        List<ParticipantStatus> out = new ArrayList<>();
        accs.forEach((pid, a) -> {
            boolean stuck = a.stuckAt != null
                    && (a.resolvedAt == null || a.stuckAt.isAfter(a.resolvedAt));
            out.add(new ParticipantStatus(
                    UserId.of(pid), a.problemId, a.lastActivityAt,
                    a.lastBuildKind, a.lastBuildAt, a.lastBuildDetail, stuck));
        });
        return out;
    }

    /** Mutable per-participant accumulator used while reducing the event stream. */
    private static final class Acc {
        String problemId;
        LocalDateTime lastActivityAt;
        ActivityKind lastBuildKind;
        LocalDateTime lastBuildAt;
        String lastBuildDetail;
        LocalDateTime stuckAt;
        LocalDateTime resolvedAt;
    }
}
