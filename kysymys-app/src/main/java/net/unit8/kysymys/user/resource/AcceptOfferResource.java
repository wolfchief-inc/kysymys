package net.unit8.kysymys.user.resource;

import enkan.collection.Parameters;
import kotowari.restful.Decision;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.system.KysymysEventBus;
import net.unit8.kysymys.user.behavior.AcceptFollow;
import net.unit8.kysymys.user.behavior.PrincipalRegistration;
import net.unit8.kysymys.user.data.OfferId;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;

import java.security.Principal;
import java.util.Map;

import static kotowari.restful.DecisionPoint.*;

@AllowedMethods({"PUT"})
public class AcceptOfferResource {

    @Decision(AUTHORIZED)
    public boolean authorized(Principal principal, DSLContext dsl, KysymysEventBus eventBus) {
        if (principal == null) return false;
        PrincipalRegistration.ensure(principal, dsl, eventBus);
        return true;
    }

    @Decision(EXISTS)
    public boolean exists(Parameters params) {
        try { OfferId.of(params.get("id")); return true; }
        catch (IllegalArgumentException ex) { return false; }
    }

    @Decision(NEW)
    public boolean isNew() { return false; }

    @Decision(RESPOND_WITH_ENTITY)
    public boolean respondWithEntity() { return true; }

    @Decision(PUT)
    public boolean accept(Parameters params, Principal principal, DSLContext dsl) {
        OfferId offerId = OfferId.of(params.get("id"));
        UserId caller = UserId.of(principal.getName());
        return new AcceptFollow(dsl).apply(offerId, caller);
    }

    @Decision(HANDLE_OK)
    public Map<String, Object> handleOk(Parameters params) {
        return Map.of("acceptedOfferId", params.get("id"));
    }
}
