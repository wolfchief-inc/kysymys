package net.unit8.kysymys.user.behavior;

import net.unit8.kysymys.user.dao.UserDao;
import net.unit8.kysymys.user.data.Role;
import net.unit8.kysymys.user.data.User;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;

import java.util.Optional;

public class GrantTeacherRole {
    private final DSLContext dsl;

    public GrantTeacherRole(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Optional<User> apply(UserId targetUserId) {
        UserDao dao = new UserDao(dsl);
        Optional<User> existing = dao.findById(targetUserId);
        if (existing.isEmpty()) return Optional.empty();
        if (existing.get().roles().contains(Role.TEACHER)) {
            return existing;
        }
        User updated = new User(
                existing.get().id(),
                existing.get().email(),
                existing.get().name(),
                existing.get().roles().withAdded(Role.TEACHER));
        dsl.transaction(cfg -> new UserDao(cfg.dsl()).upsert(updated));
        return Optional.of(updated);
    }
}
