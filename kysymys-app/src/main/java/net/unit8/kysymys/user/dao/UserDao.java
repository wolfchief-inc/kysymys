package net.unit8.kysymys.user.dao;

import net.unit8.kysymys.user.data.*;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

/**
 * jOOQ-backed repository for {@code users} and {@code user_roles}.
 * Caller manages transactions.
 */
public class UserDao {

    private static final Field<String> ID = field("id", String.class);
    private static final Field<String> EMAIL = field("email", String.class);
    private static final Field<String> NAME = field("name", String.class);
    // user_roles columns
    private static final Field<String> ROLES_USER_ID = field("user_id", String.class);
    private static final Field<String> ROLES_ROLES = field("roles", String.class);

    private final DSLContext dsl;

    public UserDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * Inserts the user row if missing, updates email/name if present, and
     * synchronises the {@code user_roles} table (deletes the entries that no
     * longer apply, inserts new ones). Roles are stored one-per-row.
     */
    public void upsert(User user) {
        int updated = dsl.update(table("users"))
                .set(EMAIL, user.email().value())
                .set(NAME, user.name().value())
                .where(ID.eq(user.id().value()))
                .execute();
        if (updated == 0) {
            dsl.insertInto(table("users"), ID, EMAIL, NAME)
                    .values(user.id().value(), user.email().value(), user.name().value())
                    .execute();
        }
        // Synchronise roles: delete existing, re-insert
        dsl.deleteFrom(table("user_roles"))
                .where(ROLES_USER_ID.eq(user.id().value()))
                .execute();
        for (Role role : user.roles().values()) {
            dsl.insertInto(table("user_roles"), ROLES_USER_ID, ROLES_ROLES)
                    .values(user.id().value(), role.name())
                    .execute();
        }
    }

    public Optional<User> findById(UserId id) {
        Record rec = dsl.select(ID, EMAIL, NAME)
                .from(table("users"))
                .where(ID.eq(id.value()))
                .fetchOne();
        if (rec == null) return Optional.empty();
        return Optional.of(new User(
                UserId.of(rec.get(ID)),
                EmailAddress.of(rec.get(EMAIL)),
                UserName.of(rec.get(NAME)),
                loadRoles(id)
        ));
    }

    public Optional<User> findByEmail(EmailAddress email) {
        Record rec = dsl.select(ID, EMAIL, NAME)
                .from(table("users"))
                .where(EMAIL.eq(email.value()))
                .fetchOne();
        if (rec == null) return Optional.empty();
        UserId id = UserId.of(rec.get(ID));
        return Optional.of(new User(
                id,
                EmailAddress.of(rec.get(EMAIL)),
                UserName.of(rec.get(NAME)),
                loadRoles(id)
        ));
    }

    public List<User> list(@Nullable String query) {
        var select = dsl.select(ID, EMAIL, NAME).from(table("users"));
        if (query == null || query.isBlank()) {
            return select.fetch().stream().map(this::mapWithRoles).toList();
        }
        String like = "%" + query.toLowerCase() + "%";
        return select.where(field("lower(name)", String.class).like(like))
                .fetch().stream().map(this::mapWithRoles).toList();
    }

    public List<User> listByRole(Role role) {
        return dsl.select(ID, EMAIL, NAME)
                .from(table("users"))
                .join(table("user_roles")).on(ROLES_USER_ID.eq(ID))
                .where(ROLES_ROLES.eq(role.name()))
                .fetch().stream().map(this::mapWithRoles).toList();
    }

    private User mapWithRoles(Record r) {
        UserId id = UserId.of(r.get(ID));
        return new User(id,
                EmailAddress.of(r.get(EMAIL)),
                UserName.of(r.get(NAME)),
                loadRoles(id));
    }

    private Roles loadRoles(UserId userId) {
        Set<String> names = dsl.select(ROLES_ROLES).from(table("user_roles"))
                .where(ROLES_USER_ID.eq(userId.value()))
                .fetch(ROLES_ROLES).stream()
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (names.isEmpty()) {
            return Roles.of(Set.of(Role.STUDENT));
        }
        return Roles.fromNames(names);
    }
}
