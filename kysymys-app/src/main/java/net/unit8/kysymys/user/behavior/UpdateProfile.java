package net.unit8.kysymys.user.behavior;

import net.unit8.kysymys.user.dao.UserDao;
import net.unit8.kysymys.user.data.*;
import org.jooq.DSLContext;
import org.jspecify.annotations.Nullable;

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

        User current = existing.get();
        @Nullable EmailAddress email = in.email();
        @Nullable UserName name = in.name();
        User updated = new User(
                current.id(),
                email != null ? email : current.email(),
                name != null ? name : current.name(),
                current.roles());
        dsl.transaction(cfg -> new UserDao(cfg.dsl()).upsert(updated));
        return Optional.of(updated);
    }

    public record Input(UserId userId, @Nullable EmailAddress email, @Nullable UserName name) {}
}
