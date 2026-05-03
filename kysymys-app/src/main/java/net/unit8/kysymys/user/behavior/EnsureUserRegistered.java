package net.unit8.kysymys.user.behavior;

import net.unit8.kysymys.events.UserCreatedEvent;
import net.unit8.kysymys.system.KysymysEventBus;
import net.unit8.kysymys.user.dao.UserDao;
import net.unit8.kysymys.user.data.*;
import org.jooq.DSLContext;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Lazy signup: looks up the user by id; if missing, inserts a fresh row and
 * publishes {@link UserCreatedEvent}. Returns the persisted/loaded user.
 */
public class EnsureUserRegistered {
    private final DSLContext dsl;
    private final KysymysEventBus eventBus;

    public EnsureUserRegistered(DSLContext dsl, KysymysEventBus eventBus) {
        this.dsl = dsl;
        this.eventBus = eventBus;
    }

    public User apply(Input in) {
        UserDao dao = new UserDao(dsl);
        Optional<User> existing = dao.findById(in.userId());
        if (existing.isPresent()) {
            return existing.get();
        }
        User fresh = new User(in.userId(), in.email(), in.name(),
                Roles.of(in.initialRoles()));
        dsl.transaction(cfg -> new UserDao(cfg.dsl()).upsert(fresh));
        if (eventBus != null) {
            eventBus.publish(new UserCreatedEvent(in.userId(), in.now()));
        }
        return fresh;
    }

    public record Input(
            UserId userId,
            EmailAddress email,
            UserName name,
            java.util.Set<Role> initialRoles,
            LocalDateTime now
    ) {}
}
