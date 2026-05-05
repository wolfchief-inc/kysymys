package net.unit8.kysymys.user.resource;

import enkan.collection.Parameters;
import kotowari.restful.Decision;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.system.KysymysEventBus;
import net.unit8.kysymys.user.behavior.PrincipalRegistration;
import net.unit8.kysymys.user.dao.UserDao;
import org.jooq.DSLContext;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import static kotowari.restful.DecisionPoint.AUTHORIZED;
import static kotowari.restful.DecisionPoint.HANDLE_OK;

@AllowedMethods({"GET"})
public class UsersResource {

    @Decision(AUTHORIZED)
    public boolean authorized(Principal principal, DSLContext dsl, KysymysEventBus eventBus) {
        if (principal == null) return false;
        PrincipalRegistration.ensure(principal, dsl, eventBus);
        return true;
    }

    @Decision(HANDLE_OK)
    public List<Map<String, Object>> list(Parameters params, DSLContext dsl) {
        String q = params.get("q");
        return new UserDao(dsl).list(q).stream()
                .map(UserJsonEncoders::encodeUser)
                .toList();
    }
}
