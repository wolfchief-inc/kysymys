package net.unit8.kysymys.user.resource;

import kotowari.restful.Decision;
import kotowari.restful.data.ContextKey;
import kotowari.restful.data.Problem;
import kotowari.restful.data.RestContext;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.system.KysymysEventBus;
import net.unit8.kysymys.system.Problems;
import net.unit8.kysymys.system.RestContexts;
import net.unit8.kysymys.user.behavior.OfferToFollow;
import net.unit8.kysymys.user.behavior.PrincipalRegistration;
import net.unit8.kysymys.user.dao.OfferDao;
import net.unit8.kysymys.user.data.Offer;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;
import tools.jackson.databind.JsonNode;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import static kotowari.restful.DecisionPoint.*;

@AllowedMethods({"GET", "POST"})
public class OffersResource {

    static final ContextKey<UserId> TARGET = ContextKey.of("target", UserId.class);
    static final ContextKey<Offer> CREATED = ContextKey.of("createdOffer", Offer.class);

    @Decision(AUTHORIZED)
    public boolean authorized(@Nullable Principal principal, DSLContext dsl, KysymysEventBus eventBus) {
        if (principal == null) return false;
        PrincipalRegistration.ensure(principal, dsl, eventBus);
        return true;
    }

    @Decision(value = MALFORMED, method = {"POST"})
    public @Nullable Problem validate(JsonNode body, RestContext context) {
        return UserJsonDecoders.OFFER.decode(body).fold(
                target -> { context.put(TARGET, target); return null; },
                Problems::of);
    }

    @Decision(POST)
    public boolean offer(Principal principal, DSLContext dsl, KysymysEventBus eventBus, RestContext context) {
        UserId caller = UserId.of(principal.getName());
        return context.get(TARGET)
                .map(target -> RestContexts.stash(context, CREATED, new OfferToFollow(dsl, eventBus).apply(
                        new OfferToFollow.Input(caller, target, LocalDateTime.now()))))
                .orElse(false);
    }

    @Decision(HANDLE_CREATED)
    public Map<String, Object> handleCreated(RestContext context) {
        return context.get(CREATED).map(UserJsonEncoders::encodeOffer).orElseThrow();
    }

    @Decision(HANDLE_OK)
    public List<Map<String, Object>> list(Principal principal, DSLContext dsl) {
        UserId caller = UserId.of(principal.getName());
        return new OfferDao(dsl).listByTarget(caller).stream()
                .map(UserJsonEncoders::encodeOffer)
                .toList();
    }
}
