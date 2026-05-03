package net.unit8.kysymys.user.resource;

import enkan.collection.Parameters;
import kotowari.restful.Decision;
import kotowari.restful.data.ContextKey;
import kotowari.restful.data.Problem;
import kotowari.restful.data.RestContext;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.system.KysymysEventBus;
import net.unit8.kysymys.user.behavior.PrincipalRegistration;
import net.unit8.kysymys.user.behavior.UpdateProfile;
import net.unit8.kysymys.user.dao.UserDao;
import net.unit8.kysymys.user.data.User;
import net.unit8.kysymys.user.data.UserId;
import net.unit8.raoh.Err;
import net.unit8.raoh.Ok;
import net.unit8.raoh.Result;
import org.jooq.DSLContext;
import tools.jackson.databind.JsonNode;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static kotowari.restful.DecisionPoint.*;

@AllowedMethods({"GET", "PUT"})
public class UserResource {

    static final ContextKey<User> USER = ContextKey.of("user", User.class);
    static final ContextKey<UserJsonDecoders.UpdateProfileInput> INPUT =
            ContextKey.of("updateInput", UserJsonDecoders.UpdateProfileInput.class);

    @Decision(AUTHORIZED)
    public boolean authorized(Principal principal, DSLContext dsl, KysymysEventBus eventBus) {
        if (principal == null) return false;
        PrincipalRegistration.ensure(principal, dsl, eventBus);
        return true;
    }

    @Decision(value = ALLOWED, method = {"PUT"})
    public boolean ownProfileOnly(Parameters params, Principal principal) {
        return principal.getName().equals(params.get("id"));
    }

    @Decision(EXISTS)
    public boolean exists(Parameters params, DSLContext dsl, RestContext context) {
        UserId id;
        try { id = UserId.of(params.get("id")); }
        catch (IllegalArgumentException ex) { return false; }
        Optional<User> u = new UserDao(dsl).findById(id);
        if (u.isEmpty()) return false;
        context.put(USER, u.get());
        return true;
    }

    @Decision(value = MALFORMED, method = {"PUT"})
    public Problem validate(JsonNode body, RestContext context) {
        Result<UserJsonDecoders.UpdateProfileInput> result = UserJsonDecoders.UPDATE_PROFILE.decode(body);
        if (result instanceof Ok<UserJsonDecoders.UpdateProfileInput> ok) {
            context.put(INPUT, ok.value());
            return null;
        }
        Err<UserJsonDecoders.UpdateProfileInput> err = (Err<UserJsonDecoders.UpdateProfileInput>) result;
        List<Problem.Violation> violations = err.issues().asList().stream()
                .map(i -> new Problem.Violation(i.path().toJsonPointer(), i.code(), i.message()))
                .toList();
        return Problem.fromViolationList(violations);
    }

    @Decision(PUT)
    public boolean update(DSLContext dsl, RestContext context) {
        UserJsonDecoders.UpdateProfileInput input = context.get(INPUT).orElseThrow();
        User existing = context.get(USER).orElseThrow();
        Optional<User> updated = new UpdateProfile(dsl).apply(
                new UpdateProfile.Input(existing.id(), input.email(), input.name()));
        updated.ifPresent(u -> context.put(USER, u));
        return updated.isPresent();
    }

    @Decision(NEW)
    public boolean isNew() { return false; }

    @Decision(RESPOND_WITH_ENTITY)
    public boolean respondWithEntity() { return true; }

    @Decision(HANDLE_OK)
    public Map<String, Object> show(RestContext context) {
        return UserJsonEncoders.encode(context.get(USER).orElseThrow());
    }
}
