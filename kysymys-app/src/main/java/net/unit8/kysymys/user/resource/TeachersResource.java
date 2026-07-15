package net.unit8.kysymys.user.resource;

import kotowari.restful.Decision;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.system.KysymysEventBus;
import net.unit8.kysymys.user.behavior.PrincipalRegistration;
import net.unit8.kysymys.user.dao.UserDao;
import net.unit8.kysymys.user.data.Role;
import org.jooq.DSLContext;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import static kotowari.restful.DecisionPoint.AUTHORIZED;
import static kotowari.restful.DecisionPoint.HANDLE_OK;

@AllowedMethods({"GET"})
public class TeachersResource {

    @Decision(AUTHORIZED)
    public boolean authorized(@Nullable Principal principal, DSLContext dsl, KysymysEventBus eventBus) {
        if (principal == null) return false;
        PrincipalRegistration.ensure(principal, dsl, eventBus);
        return true;
    }

    @Decision(HANDLE_OK)
    public List<Map<String, Object>> list(DSLContext dsl) {
        return new UserDao(dsl).listByRole(Role.TEACHER).stream()
                .map(UserJsonEncoders::encodeUser)
                .toList();
    }
}
