package net.unit8.kysymys.user.resource;

import enkan.collection.Parameters;
import kotowari.restful.Decision;
import kotowari.restful.data.ContextKey;
import kotowari.restful.data.Problem;
import kotowari.restful.data.RestContext;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.system.KysymysEventBus;
import net.unit8.kysymys.system.Optionals;
import net.unit8.kysymys.system.Problems;
import net.unit8.kysymys.system.RestContexts;
import net.unit8.kysymys.user.behavior.PrincipalRegistration;
import net.unit8.kysymys.user.behavior.UpdateProfile;
import net.unit8.kysymys.user.dao.UserDao;
import net.unit8.kysymys.user.data.User;
import org.jooq.DSLContext;
import tools.jackson.databind.JsonNode;

import java.security.Principal;
import java.util.Map;

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
        return UserPathDecoders.USER_ID.decode(params.get("id"))
                .fold(id -> RestContexts.stash(context, USER, new UserDao(dsl).findById(id)), _ -> false);
    }

    @Decision(value = MALFORMED, method = {"PUT"})
    public Problem validate(JsonNode body, RestContext context) {
        return UserJsonDecoders.UPDATE_PROFILE.decode(body).fold(
                input -> { context.put(INPUT, input); return null; },
                Problems::of);
    }

    @Decision(PUT)
    public boolean update(DSLContext dsl, RestContext context) {
        return Optionals.map2(context.get(INPUT), context.get(USER),
                        (input, existing) -> RestContexts.stash(context, USER, new UpdateProfile(dsl).apply(
                                new UpdateProfile.Input(existing.id(), input.email(), input.name()))))
                .orElse(false);
    }

    @Decision(NEW)
    public boolean isNew() { return false; }

    @Decision(RESPOND_WITH_ENTITY)
    public boolean respondWithEntity() { return true; }

    @Decision(HANDLE_OK)
    public Map<String, Object> show(RestContext context) {
        return context.get(USER).map(UserJsonEncoders::encodeUser).orElseThrow();
    }
}
