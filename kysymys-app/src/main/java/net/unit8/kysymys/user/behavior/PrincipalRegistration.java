package net.unit8.kysymys.user.behavior;

import enkan.security.bouncr.UserPermissionPrincipal;
import net.unit8.kysymys.system.KysymysEventBus;
import net.unit8.kysymys.user.data.*;
import org.jooq.DSLContext;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Helper that turns an authenticated {@link Principal} into a registered user
 * on demand. Resource classes call {@link #ensure} from inside the AUTHORIZED
 * (or any later) decision so that the {@code users} row exists by the time
 * downstream daos need to reference it.
 *
 * <p>Required JWT claims beyond the Bouncr defaults: {@code email} and
 * {@code name}. Missing claims fall back to placeholder values.
 */
public final class PrincipalRegistration {
    private PrincipalRegistration() {}

    public static User ensure(Principal principal, DSLContext dsl, KysymysEventBus eventBus) {
        UserId userId = UserId.of(principal.getName());
        EmailAddress email = pickEmail(principal, userId);
        UserName name = pickName(principal, userId);
        Set<Role> roles = pickRoles(principal);

        return new EnsureUserRegistered(dsl, eventBus).apply(
                new EnsureUserRegistered.Input(
                        userId, email, name, roles, LocalDateTime.now()));
    }

    private static EmailAddress pickEmail(Principal principal, UserId userId) {
        if (principal instanceof UserPermissionPrincipal p && p.profiles() != null) {
            Object claim = p.profiles().get("email");
            if (claim instanceof String s && !s.isBlank()) {
                try { return EmailAddress.of(s); } catch (RuntimeException ignored) {}
            }
        }
        // Fallback: synthesise a placeholder so the row can still be inserted.
        return EmailAddress.of(userId.value() + "@unknown.kysymys.local");
    }

    private static UserName pickName(Principal principal, UserId userId) {
        if (principal instanceof UserPermissionPrincipal p && p.profiles() != null) {
            Object claim = p.profiles().get("name");
            if (claim instanceof String s && !s.isBlank()) {
                try { return UserName.of(s); } catch (RuntimeException ignored) {}
            }
        }
        return UserName.of(userId.value());
    }

    private static Set<Role> pickRoles(Principal principal) {
        if (principal instanceof UserPermissionPrincipal p) {
            java.util.Set<Role> rs = new java.util.LinkedHashSet<>();
            for (String perm : p.permissions()) {
                try { rs.add(Role.valueOf(perm)); } catch (IllegalArgumentException ignored) {}
            }
            if (!rs.isEmpty()) return rs;
        }
        return Set.of(Role.STUDENT);
    }
}
