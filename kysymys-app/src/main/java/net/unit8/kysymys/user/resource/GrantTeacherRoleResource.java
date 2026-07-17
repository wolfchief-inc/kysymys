package net.unit8.kysymys.user.resource;

import kotowari.restful.Decision;
import kotowari.restful.data.ContextKey;
import kotowari.restful.data.Problem;
import kotowari.restful.data.RestContext;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.system.KysymysEventBus;
import net.unit8.kysymys.system.Problems;
import net.unit8.kysymys.system.RestContexts;
import net.unit8.kysymys.user.behavior.GrantTeacherRole;
import net.unit8.kysymys.user.behavior.PrincipalRegistration;
import net.unit8.kysymys.user.data.User;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;
import tools.jackson.databind.JsonNode;

import java.security.Principal;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import static kotowari.restful.DecisionPoint.*;

@AllowedMethods({"POST"})
public class GrantTeacherRoleResource {

    static final ContextKey<UserId> TARGET = ContextKey.of("target", UserId.class);
    static final ContextKey<User> RESULT = ContextKey.of("result", User.class);

    @Decision(AUTHORIZED)
    public boolean authorized(@Nullable Principal principal, DSLContext dsl, KysymysEventBus eventBus) {
        if (principal == null) return false;
        PrincipalRegistration.ensure(principal, dsl, eventBus);
        return true;
    }

    @Decision(ALLOWED)
    public boolean teacherOnly(Principal principal) {
        return principal instanceof enkan.security.bouncr.UserPermissionPrincipal p
                && p.permissions().contains("TEACHER");
    }

    @Decision(MALFORMED)
    public @Nullable Problem validate(JsonNode body, RestContext context) {
        return UserJsonDecoders.GRANT_TEACHER_ROLE.decode(body).fold(
                target -> { context.put(TARGET, target); return null; },
                Problems::of);
    }

    @Decision(POST)
    public boolean grant(DSLContext dsl, RestContext context) {
        return context.get(TARGET)
                .map(target -> RestContexts.stash(context, RESULT, new GrantTeacherRole(dsl).apply(target)))
                .orElse(false);
    }

    @Decision(HANDLE_CREATED)
    public Map<String, Object> handleCreated(RestContext context) {
        return context.get(RESULT).map(UserJsonEncoders::encodeUser).orElseThrow();
    }
}
