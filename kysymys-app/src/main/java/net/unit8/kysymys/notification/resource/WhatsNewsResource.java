package net.unit8.kysymys.notification.resource;

import enkan.collection.Parameters;
import kotowari.restful.Decision;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.notification.behavior.MarkAsRead;
import net.unit8.kysymys.notification.dao.WhatsNewDao;
import net.unit8.kysymys.system.KysymysEventBus;
import net.unit8.kysymys.user.behavior.PrincipalRegistration;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;

import java.security.Principal;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import static kotowari.restful.DecisionPoint.AUTHORIZED;
import static kotowari.restful.DecisionPoint.HANDLE_OK;
import static kotowari.restful.DecisionPoint.PUT;
import static kotowari.restful.DecisionPoint.NEW;
import static kotowari.restful.DecisionPoint.RESPOND_WITH_ENTITY;
import static kotowari.restful.DecisionPoint.EXISTS;

@AllowedMethods({"GET", "PUT"})
public class WhatsNewsResource {

    @Decision(AUTHORIZED)
    public boolean authorized(@Nullable Principal principal, DSLContext dsl, KysymysEventBus eventBus) {
        if (principal == null) return false;
        PrincipalRegistration.ensure(principal, dsl, eventBus);
        return true;
    }

    @Decision(value = EXISTS, method = {"PUT"})
    public boolean exists(Parameters params) {
        return NotificationPathDecoders.WHATS_NEW_ID.decode(params.get("id")).isOk();
    }

    @Decision(value = NEW, method = {"PUT"})
    public boolean isNew() { return false; }

    @Decision(value = RESPOND_WITH_ENTITY, method = {"PUT"})
    public boolean respondWithEntity() { return true; }

    @Decision(PUT)
    public boolean markRead(Parameters params, Principal principal, DSLContext dsl) {
        UserId caller = UserId.of(principal.getName());
        return NotificationPathDecoders.WHATS_NEW_ID.decode(params.get("id"))
                .fold(id -> new MarkAsRead(dsl).apply(id, caller), _ -> false);
    }

    @Decision(HANDLE_OK)
    public Object handleOk(Parameters params, Principal principal, DSLContext dsl) {
        // GET path: return list. PUT path: return acknowledgement.
        if (params.get("id") != null) {
            return Map.of("readWhatsNewId", params.get("id"));
        }
        UserId caller = UserId.of(principal.getName());
        WhatsNewDao dao = new WhatsNewDao(dsl);
        return dao.listByUser(caller).stream()
                .map(w -> NotificationJsonEncoders.encodeWhatsNew(w, dao.isUnread(w.id(), caller)))
                .toList();
    }
}
