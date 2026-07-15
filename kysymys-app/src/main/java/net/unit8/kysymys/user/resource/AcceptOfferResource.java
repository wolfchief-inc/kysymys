package net.unit8.kysymys.user.resource;

import enkan.collection.Parameters;
import kotowari.restful.Decision;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.system.KysymysEventBus;
import net.unit8.kysymys.user.behavior.AcceptFollow;
import net.unit8.kysymys.user.behavior.PrincipalRegistration;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;

import java.security.Principal;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import static kotowari.restful.DecisionPoint.*;

@AllowedMethods({"PUT"})
public class AcceptOfferResource {

    @Decision(AUTHORIZED)
    public boolean authorized(@Nullable Principal principal, DSLContext dsl, KysymysEventBus eventBus) {
        if (principal == null) return false;
        PrincipalRegistration.ensure(principal, dsl, eventBus);
        return true;
    }

    @Decision(EXISTS)
    public boolean exists(Parameters params) {
        return UserPathDecoders.OFFER_ID.decode(params.get("id")).isOk();
    }

    @Decision(NEW)
    public boolean isNew() { return false; }

    @Decision(RESPOND_WITH_ENTITY)
    public boolean respondWithEntity() { return true; }

    @Decision(PUT)
    public boolean accept(Parameters params, Principal principal, DSLContext dsl) {
        UserId caller = UserId.of(principal.getName());
        return UserPathDecoders.OFFER_ID.decode(params.get("id"))
                .fold(offerId -> new AcceptFollow(dsl).apply(offerId, caller), _ -> false);
    }

    @Decision(HANDLE_OK)
    public Map<String, Object> handleOk(Parameters params) {
        return Map.of("acceptedOfferId", params.get("id"));
    }
}
