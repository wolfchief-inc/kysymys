package net.unit8.kysymys.activity.behavior;

import net.unit8.kysymys.activity.dao.ActivityEventDao;
import net.unit8.kysymys.activity.data.ActivityEvent;
import net.unit8.kysymys.activity.data.ActivityEventId;
import net.unit8.kysymys.activity.data.ActivityKind;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;

import java.time.LocalDateTime;

/** Records one telemetry event reported by a participant's machine. */
public class RecordActivity {
    private final DSLContext dsl;

    public RecordActivity(DSLContext dsl) {
        this.dsl = dsl;
    }

    public ActivityEvent apply(Input in) {
        ActivityEvent event = new ActivityEvent(
                ActivityEventId.newId(),
                in.participantId(),
                in.problemId(),
                in.kind(),
                in.detail(),
                in.now());
        new ActivityEventDao(dsl).insert(event);
        return event;
    }

    public record Input(
            UserId participantId,
            String problemId,
            ActivityKind kind,
            String detail,
            LocalDateTime now
    ) {
    }
}
