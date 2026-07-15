package net.unit8.kysymys.notification.dao;

import net.unit8.kysymys.notification.data.UnreadWhatsNew;
import net.unit8.kysymys.notification.data.WhatsNew;
import net.unit8.kysymys.notification.data.WhatsNewId;
import net.unit8.kysymys.user.data.UserId;
import net.unit8.raoh.Result;
import org.jooq.DSLContext;
import org.jooq.Field;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class WhatsNewDao {

    private static final ObjectMapper JSON = new ObjectMapper();

    private static final Field<String> ID = field("id", String.class);
    private static final Field<String> USER_ID = field("user_id", String.class);
    private static final Field<String> TEMPLATE_PATH = field("template_path", String.class);
    private static final Field<String> PARAMS = field("params", String.class);
    private static final Field<LocalDateTime> POSTED_AT = field("posted_at", LocalDateTime.class);

    private static final Field<String> UNREAD_ID = field("id", String.class);
    private static final Field<String> WHATS_NEW_ID = field("whats_new_id", String.class);
    private static final Field<String> UNREAD_USER_ID = field("user_id", String.class);

    private final DSLContext dsl;

    public WhatsNewDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void insert(WhatsNew w) {
        String params = serialiseParams(w.params());
        dsl.insertInto(table("whats_news"), ID, USER_ID, TEMPLATE_PATH, PARAMS, POSTED_AT)
                .values(w.id().value(), w.userId().value(), w.templatePath().value(), params, w.postedAt())
                .execute();
    }

    public void insertUnread(UnreadWhatsNew u) {
        dsl.insertInto(table("unread_whats_news"), UNREAD_ID, WHATS_NEW_ID, UNREAD_USER_ID)
                .values(u.id().value(), u.whatsNewId().value(), u.userId().value())
                .execute();
    }

    public List<WhatsNew> listByUser(UserId userId) {
        return Result.traverse(
                dsl.select(ID, USER_ID, TEMPLATE_PATH, PARAMS, POSTED_AT)
                        .from(table("whats_news"))
                        .where(USER_ID.eq(userId.value()))
                        .orderBy(POSTED_AT.desc())
                        .fetch(),
                NotificationRecordDecoders.WHATS_NEW::decode).getOrThrow();
    }

    /** Deletes the unread row for {@code whatsNewId} owned by {@code userId}. Returns rows affected. */
    public int markRead(WhatsNewId whatsNewId, UserId userId) {
        return dsl.deleteFrom(table("unread_whats_news"))
                .where(WHATS_NEW_ID.eq(whatsNewId.value()))
                .and(UNREAD_USER_ID.eq(userId.value()))
                .execute();
    }

    public boolean isUnread(WhatsNewId whatsNewId, UserId userId) {
        return dsl.selectCount().from(table("unread_whats_news"))
                .where(WHATS_NEW_ID.eq(whatsNewId.value()))
                .and(UNREAD_USER_ID.eq(userId.value()))
                .fetchOne(0, Long.class) > 0;
    }

    private static String serialiseParams(Map<String, Object> params) {
        if (params.isEmpty()) return "{}";
        try {
            return JSON.writeValueAsString(params);
        } catch (Exception e) {
            throw new UncheckedIOException(new IOException(e));
        }
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> deserialiseParams(String json) {
        if (json.isBlank()) return Map.of();
        try {
            return JSON.readValue(json, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }
}
