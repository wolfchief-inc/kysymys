package net.unit8.kysymys.user.dao;

import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;
import org.jooq.Field;

import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class ConnectionDao {
    private static final Field<String> FOLLOWEE_ID = field("followee_id", String.class);
    private static final Field<String> FOLLOWER_ID = field("follower_id", String.class);

    private final DSLContext dsl;

    public ConnectionDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    /** Inserts a connection if missing (idempotent via PK constraint). */
    public void add(UserId followee, UserId follower) {
        long exists = dsl.selectCount().from(table("connections"))
                .where(FOLLOWEE_ID.eq(followee.value()))
                .and(FOLLOWER_ID.eq(follower.value()))
                .fetchOne(0, Long.class);
        if (exists == 0) {
            dsl.insertInto(table("connections"), FOLLOWEE_ID, FOLLOWER_ID)
                    .values(followee.value(), follower.value())
                    .execute();
        }
    }

    /** Lists the userIds who follow {@code followee}. */
    public List<UserId> listFollowersOf(UserId followee) {
        return dsl.select(FOLLOWER_ID).from(table("connections"))
                .where(FOLLOWEE_ID.eq(followee.value()))
                .fetch(FOLLOWER_ID).stream()
                .map(UserId::of)
                .toList();
    }

    /** Lists the userIds whom {@code follower} follows. */
    public List<UserId> listFolloweesOf(UserId follower) {
        return dsl.select(FOLLOWEE_ID).from(table("connections"))
                .where(FOLLOWER_ID.eq(follower.value()))
                .fetch(FOLLOWEE_ID).stream()
                .map(UserId::of)
                .toList();
    }

    public boolean isFollowing(UserId follower, UserId followee) {
        return dsl.selectCount().from(table("connections"))
                .where(FOLLOWEE_ID.eq(followee.value()))
                .and(FOLLOWER_ID.eq(follower.value()))
                .fetchOne(0, Long.class) > 0;
    }
}
