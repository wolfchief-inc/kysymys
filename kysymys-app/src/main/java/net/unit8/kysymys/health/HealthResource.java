package net.unit8.kysymys.health;

import kotowari.restful.Decision;
import kotowari.restful.resource.AllowedMethods;
import org.jooq.DSLContext;

import java.util.Map;

import static kotowari.restful.DecisionPoint.HANDLE_OK;
import static org.jooq.impl.DSL.table;

/**
 * Liveness probe. Returns {@code 200 OK} with the row count of the {@code users}
 * table so that Sub-A end-to-end verification covers Flyway application and the
 * jOOQ DSLContext binding in a single request.
 *
 * <p>No authentication is required; this resource is reachable without the
 * {@code x-bouncr-credential} header so monitors can hit it without a token.
 */
@AllowedMethods({"GET"})
public class HealthResource {

    @Decision(HANDLE_OK)
    public Map<String, Object> health(DSLContext dsl) {
        long userCount = dsl.selectCount().from(table("users")).fetchOne(0, Long.class);
        return Map.of(
                "status", "ok",
                "userCount", userCount
        );
    }
}
