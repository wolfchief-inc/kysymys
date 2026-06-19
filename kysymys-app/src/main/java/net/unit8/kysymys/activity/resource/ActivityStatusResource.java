package net.unit8.kysymys.activity.resource;

import enkan.security.bouncr.UserPermissionPrincipal;
import kotowari.restful.Decision;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.activity.behavior.ListParticipantStatus;
import org.jooq.DSLContext;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static kotowari.restful.DecisionPoint.ALLOWED;
import static kotowari.restful.DecisionPoint.AUTHORIZED;
import static kotowari.restful.DecisionPoint.HANDLE_OK;

/**
 * {@code GET /activity/status} — the instructor view. Returns the current work
 * state of every participant so the dashboard can surface who needs help.
 * Teacher-only, reusing the same {@code TEACHER} permission as
 * {@code GrantTeacherRoleResource}.
 *
 * <p>The response carries {@code serverTime} so the dashboard measures idle time
 * against the server clock rather than the browser's, avoiding skew.
 */
@AllowedMethods({"GET"})
public class ActivityStatusResource {

    @Decision(AUTHORIZED)
    public boolean authorized(Principal principal) {
        return principal != null;
    }

    @Decision(ALLOWED)
    public boolean teacherOnly(Principal principal) {
        return principal instanceof UserPermissionPrincipal p
                && p.permissions().contains("TEACHER");
    }

    @Decision(HANDLE_OK)
    public Map<String, Object> status(DSLContext dsl) {
        List<Map<String, Object>> participants = new ListParticipantStatus(dsl).apply().stream()
                .map(ActivityJsonEncoders::encodeStatus)
                .toList();
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("serverTime", LocalDateTime.now().toString());
        body.put("participants", participants);
        return body;
    }
}
