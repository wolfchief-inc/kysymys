package net.unit8.kysymys.user.resource;

import kotowari.restful.Decision;
import kotowari.restful.data.ContextKey;
import kotowari.restful.data.Problem;
import kotowari.restful.data.RestContext;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.system.KysymysEventBus;
import net.unit8.kysymys.user.behavior.OfferToFollow;
import net.unit8.kysymys.user.behavior.PrincipalRegistration;
import net.unit8.kysymys.user.dao.OfferDao;
import net.unit8.kysymys.user.data.Offer;
import net.unit8.kysymys.user.data.UserId;
import net.unit8.raoh.Err;
import net.unit8.raoh.Ok;
import net.unit8.raoh.Result;
import net.unit8.raoh.decode.Decoder;
import org.jooq.DSLContext;
import tools.jackson.databind.JsonNode;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static kotowari.restful.DecisionPoint.*;
import static net.unit8.raoh.json.JsonDecoders.field;
import static net.unit8.raoh.json.JsonDecoders.string;

@AllowedMethods({"GET", "POST"})
public class OffersResource {

    private static final Decoder<JsonNode, UserId> INPUT =
            field("targetUserId", string().fixedLength(21)).map(UserId::of);

    static final ContextKey<UserId> TARGET = ContextKey.of("target", UserId.class);
    static final ContextKey<Offer> CREATED = ContextKey.of("createdOffer", Offer.class);

    @Decision(AUTHORIZED)
    public boolean authorized(Principal principal, DSLContext dsl, KysymysEventBus eventBus) {
        if (principal == null) return false;
        PrincipalRegistration.ensure(principal, dsl, eventBus);
        return true;
    }

    @Decision(value = MALFORMED, method = {"POST"})
    public Problem validate(JsonNode body, RestContext context) {
        Result<UserId> result = INPUT.decode(body);
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
    public boolean offer(Principal principal, DSLContext dsl, KysymysEventBus eventBus, RestContext context) {
        UserId target = context.get(TARGET).orElseThrow();
        UserId caller = UserId.of(principal.getName());
        Optional<Offer> created = new OfferToFollow(dsl, eventBus).apply(
                new OfferToFollow.Input(caller, target, LocalDateTime.now()));
        created.ifPresent(o -> context.put(CREATED, o));
        return created.isPresent();
    }

    @Decision(HANDLE_CREATED)
    public Map<String, Object> handleCreated(RestContext context) {
        Offer o = context.get(CREATED).orElseThrow();
        return encode(o);
    }

    @Decision(HANDLE_OK)
    public List<Map<String, Object>> list(Principal principal, DSLContext dsl) {
        UserId caller = UserId.of(principal.getName());
        return new OfferDao(dsl).listByTarget(caller).stream()
                .map(OffersResource::encode)
                .toList();
    }

    private static Map<String, Object> encode(Offer o) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", o.id().value());
        body.put("offeringUserId", o.offeringUserId().value());
        body.put("targetUserId", o.targetUserId().value());
        body.put("offeredAt", o.offeredAt().toString());
        return body;
    }
}
