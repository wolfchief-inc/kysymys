package net.unit8.kysymys.health;

import kotowari.restful.Decision;
import kotowari.restful.data.RestContext;
import kotowari.restful.resource.AllowedMethods;

import java.security.Principal;
import java.util.Map;

import static kotowari.restful.DecisionPoint.AUTHORIZED;
import static kotowari.restful.DecisionPoint.HANDLE_OK;

/**
 * Echoes the authenticated principal. Used in Sub-A to verify that the Bouncr
 * HMAC backend correctly parses the {@code x-bouncr-credential} JWT header and
 * surfaces the {@code sub} claim as a {@link Principal}. Without a valid token
 * the {@link #authorized} check fails and kotowari-restful returns 401.
 */
@AllowedMethods({"GET"})
public class MeResource {

    @Decision(AUTHORIZED)
    public boolean authorized(Principal principal) {
        return principal != null;
    }

    @Decision(HANDLE_OK)
    public Map<String, Object> me(Principal principal, RestContext context) {
        return Map.of("sub", principal.getName());
    }
}
