package net.unit8.kysymys.avatar.dao;

import net.unit8.kysymys.avatar.data.UserAvatar;
import net.unit8.kysymys.user.data.UserId;
import net.unit8.raoh.decode.Decoder;
import org.jooq.Record;

import static net.unit8.raoh.decode.ObjectDecoders.bytes;
import static net.unit8.raoh.decode.ObjectDecoders.string;
import static net.unit8.raoh.jooq.JooqRecordDecoders.combine;
import static net.unit8.raoh.jooq.JooqRecordDecoders.field;

/** raoh-jooq decoder mapping a {@code user_avatars} row into a {@link UserAvatar}. */
public final class AvatarRecordDecoders {
    private AvatarRecordDecoders() {}

    public static final Decoder<Record, UserAvatar> USER_AVATAR = combine(
            field("user_id", string()).map(UserId::of),
            field("image_content", bytes())
    ).map(UserAvatar::new);
}
