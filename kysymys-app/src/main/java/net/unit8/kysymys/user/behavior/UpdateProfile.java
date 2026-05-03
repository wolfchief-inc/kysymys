package net.unit8.kysymys.user.behavior;

import net.unit8.kysymys.user.dao.UserDao;
import net.unit8.kysymys.user.data.*;
import org.jooq.DSLContext;

import java.util.Optional;

public class UpdateProfile {
    private final DSLContext dsl;

    public UpdateProfile(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Optional<User> apply(Input in) {
        UserDao dao = new UserDao(dsl);
        Optional<User> existing = dao.findById(in.userId());
        if (existing.isEmpty()) return Optional.empty();

        User updated = new User(
                existing.get().id(),
                in.email() != null ? in.email() : existing.get().email(),
                in.name() != null ? in.name() : existing.get().name(),
                existing.get().roles());
        dsl.transaction(cfg -> new UserDao(cfg.dsl()).upsert(updated));
        return Optional.of(updated);
    }

    public record Input(UserId userId, EmailAddress email, UserName name) {}
}
