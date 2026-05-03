package net.unit8.kysymys.user.resource;

import kotowari.restful.Decision;
import kotowari.restful.data.ContextKey;
import kotowari.restful.data.Problem;
import kotowari.restful.data.RestContext;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.system.KysymysEventBus;
import net.unit8.kysymys.user.behavior.GrantTeacherRole;
import net.unit8.kysymys.user.behavior.PrincipalRegistration;
import net.unit8.kysymys.user.data.User;
import net.unit8.kysymys.user.data.UserId;
import net.unit8.raoh.Err;
import net.unit8.raoh.Ok;
import net.unit8.raoh.Result;
import net.unit8.raoh.decode.Decoder;
import org.jooq.DSLContext;
import tools.jackson.databind.JsonNode;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static kotowari.restful.DecisionPoint.*;
import static net.unit8.raoh.json.JsonDecoders.field;
import static net.unit8.raoh.json.JsonDecoders.string;

@AllowedMethods({"POST"})
public class GrantTeacherRoleResource {

    private static final Decoder<JsonNode, UserId> INPUT_DECODER =
            field("targetUserId", string().fixedLength(21)).map(UserId::of);

    static final ContextKey<UserId> TARGET = ContextKey.of("target", UserId.class);
    static final ContextKey<User> RESULT = ContextKey.of("result", User.class);

    @Decision(AUTHORIZED)
    public boolean authorized(Principal principal, DSLContext dsl, KysymysEventBus eventBus) {
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
    public Problem validate(JsonNode body, RestContext context) {
        Result<UserId> result = INPUT_DECODER.decode(body);
        if (result instanceof Ok<UserId> ok) {
            context.put(TARGET, ok.value());
            return null;
        }
        Err<UserId> err = (Err<UserId>) result;
        List<Problem.Violation> violations = err.issues().asList().stream()
                .map(i -> new Problem.Violation(i.path().toJsonPointer(), i.code(), i.message()))
                .toList();
        return Problem.fromViolationList(violations);
    }

    @Decision(POST)
    public boolean grant(DSLContext dsl, RestContext context) {
        UserId target = context.get(TARGET).orElseThrow();
        Optional<User> updated = new GrantTeacherRole(dsl).apply(target);
        updated.ifPresent(u -> context.put(RESULT, u));
        return updated.isPresent();
    }

    @Decision(HANDLE_CREATED)
    public Map<String, Object> handleCreated(RestContext context) {
        return UserJsonEncoders.encode(context.get(RESULT).orElseThrow());
    }
}
