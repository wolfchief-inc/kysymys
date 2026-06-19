package net.unit8.kysymys.activity.behavior;

import net.unit8.kysymys.activity.dao.ActivityEventDao;
import net.unit8.kysymys.activity.data.ParticipantStatus;
import org.jooq.DSLContext;

import java.util.List;

/** Returns the current work state of every participant for the instructor dashboard. */
public class ListParticipantStatus {
    private final DSLContext dsl;

    public ListParticipantStatus(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<ParticipantStatus> apply() {
        return new ActivityEventDao(dsl).listStatuses();
    }
}
