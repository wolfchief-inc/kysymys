package net.unit8.kysymys.avatar.dao;

import net.unit8.kysymys.avatar.data.UserAvatar;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;

import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class UserAvatarDao {
    private static final Field<String> USER_ID = field("user_id", String.class);
    private static final Field<Long> AVATAR_CODE = field("avatar_code", Long.class);
    private static final Field<byte[]> IMAGE_CONTENT = field("image_content", byte[].class);

    private final DSLContext dsl;

    public UserAvatarDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Optional<UserAvatar> findByUser(UserId userId) {
        Record rec = dsl.select(USER_ID, IMAGE_CONTENT)
                .from(table("user_avatars"))
                .where(USER_ID.eq(userId.value()))
                .fetchOne();
        if (rec == null) return Optional.empty();
        byte[] image = rec.get(IMAGE_CONTENT);
        if (image == null || image.length == 0) return Optional.empty();
        return Optional.of(AvatarRecordDecoders.USER_AVATAR.decode(rec).getOrThrow());
    }

    public void upsert(UserAvatar avatar) {
        int updated = dsl.update(table("user_avatars"))
                .set(IMAGE_CONTENT, avatar.image())
                .where(USER_ID.eq(avatar.userId().value()))
                .execute();
        if (updated == 0) {
            dsl.insertInto(table("user_avatars"), USER_ID, AVATAR_CODE, IMAGE_CONTENT)
                    .values(avatar.userId().value(), null, avatar.image())
                    .execute();
        }
    }
}
