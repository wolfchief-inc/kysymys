package net.unit8.kysymys.activity.resource;

import kotowari.restful.Decision;
import kotowari.restful.data.ContextKey;
import kotowari.restful.data.Problem;
import kotowari.restful.data.RestContext;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.activity.behavior.RecordActivity;
import net.unit8.kysymys.activity.data.ActivityEvent;
import net.unit8.kysymys.system.Problems;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;
import tools.jackson.databind.JsonNode;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import static kotowari.restful.DecisionPoint.AUTHORIZED;
import static kotowari.restful.DecisionPoint.HANDLE_CREATED;
import static kotowari.restful.DecisionPoint.MALFORMED;
import static kotowari.restful.DecisionPoint.POST;

/**
 * {@code POST /activity} — ingests one work-activity telemetry event from a
 * participant's machine. The participant is identified by the Bouncr principal,
 * exactly like answer submission, so a single per-person JWT ties their builds,
 * heartbeats, and stuck signals to the same identity.
 */
@AllowedMethods({"POST"})
public class ActivityResource {

    static final ContextKey<ActivityJsonDecoders.RecordActivityInput> INPUT =
            ContextKey.of("recordActivityInput", ActivityJsonDecoders.RecordActivityInput.class);
    static final ContextKey<ActivityEvent> RECORDED =
            ContextKey.of("recordedActivity", ActivityEvent.class);

    @Decision(AUTHORIZED)
    public boolean authorized(Principal principal) {
        return principal != null;
    }

    @Decision(value = MALFORMED, method = {"POST"})
    public @Nullable Problem validate(JsonNode body, RestContext context) {
        return ActivityJsonDecoders.RECORD_ACTIVITY.decode(body).fold(
                input -> { context.put(INPUT, input); return null; },
                Problems::of);
    }

    @Decision(POST)
    public boolean record(UserId caller, DSLContext dsl, RestContext context) {
        return context.get(INPUT).map(input -> {
            context.put(RECORDED, new RecordActivity(dsl).apply(new RecordActivity.Input(
                    caller, input.problemId(), input.kind(), input.detail(), LocalDateTime.now())));
            return true;
        }).orElse(false);
    }

    @Decision(HANDLE_CREATED)
    public Map<String, Object> handleCreated(RestContext context) {
        return context.get(RECORDED)
                .map(event -> Map.<String, Object>of(
                        "id", event.id().value(),
                        "kind", event.kind().name(),
                        "recordedAt", event.occurredAt().toString()))
                .orElseThrow();
    }
}
