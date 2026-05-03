package net.unit8.kysymys.avatar.behavior;

import net.unit8.kysymys.avatar.dao.UserAvatarDao;
import net.unit8.kysymys.avatar.data.UserAvatar;
import net.unit8.kysymys.avatar.image.EightBitAvatarGenerator;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;

import java.util.Optional;

/**
 * Returns the cached avatar bytes for {@code userId}; on cache miss
 * generates a fresh PNG, stores it, and returns the new bytes.
 */
public class EnsureAvatar {
    private final DSLContext dsl;
    private final EightBitAvatarGenerator generator;

    public EnsureAvatar(DSLContext dsl, EightBitAvatarGenerator generator) {
        this.dsl = dsl;
        this.generator = generator;
    }

    public byte[] apply(UserId userId) {
        UserAvatarDao dao = new UserAvatarDao(dsl);
        Optional<UserAvatar> existing = dao.findByUser(userId);
        if (existing.isPresent()) {
            return existing.get().image();
        }
        byte[] bytes = generator.generate(userId.value());
        UserAvatar avatar = new UserAvatar(userId, bytes);
        dsl.transaction(cfg -> new UserAvatarDao(cfg.dsl()).upsert(avatar));
        return bytes;
    }
}
