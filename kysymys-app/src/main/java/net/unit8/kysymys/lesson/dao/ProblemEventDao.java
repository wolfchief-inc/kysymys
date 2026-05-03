package net.unit8.kysymys.lesson.dao;

import net.unit8.kysymys.lesson.data.*;
import org.jooq.DSLContext;
import org.jooq.Field;

import java.time.LocalDateTime;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class ProblemEventDao {
    private static final Field<String> ID = field("id", String.class);
    private static final Field<String> LIFECYCLE_ID = field("problem_lifecycle_id", String.class);
    private static final Field<LocalDateTime> OCCURRED_AT = field("occurred_at", LocalDateTime.class);
    private static final Field<String> CREATOR_ID = field("creator_id", String.class);
    private static final Field<String> UPDATER_ID = field("updater_id", String.class);
    private static final Field<String> ARCHIVER_ID = field("archiver_id", String.class);

    private final DSLContext dsl;

    public ProblemEventDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void insert(ProblemEvent event) {
        switch (event) {
            case ProblemCreatedEvent c -> dsl.insertInto(table("problem_created_events"),
                            ID, LIFECYCLE_ID, OCCURRED_AT, CREATOR_ID)
                    .values(c.id().value(), c.lifecycleId().value(), c.occurredAt(), c.creatorId().value())
                    .execute();
            case ProblemUpdatedEvent u -> dsl.insertInto(table("problem_updated_events"),
                            ID, LIFECYCLE_ID, OCCURRED_AT, UPDATER_ID)
                    .values(u.id().value(), u.lifecycleId().value(), u.occurredAt(), u.updaterId().value())
                    .execute();
            case ProblemArchivedEvent a -> dsl.insertInto(table("problem_archived_events"),
                            ID, LIFECYCLE_ID, OCCURRED_AT, ARCHIVER_ID)
                    .values(a.id().value(), a.lifecycleId().value(), a.occurredAt(), a.archiverId().value())
                    .execute();
        }
    }

    public long countByLifecycle(ProblemLifecycleId lifecycleId) {
        long created = countOn("problem_created_events", lifecycleId);
        long updated = countOn("problem_updated_events", lifecycleId);
        long archived = countOn("problem_archived_events", lifecycleId);
        return created + updated + archived;
    }

    private long countOn(String tableName, ProblemLifecycleId lifecycleId) {
        return dsl.selectCount().from(table(tableName))
                .where(LIFECYCLE_ID.eq(lifecycleId.value()))
                .fetchOne(0, Long.class);
    }
}
