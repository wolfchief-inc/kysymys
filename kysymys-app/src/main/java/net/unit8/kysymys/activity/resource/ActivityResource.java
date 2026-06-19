package net.unit8.kysymys.activity.resource;

import kotowari.restful.Decision;
import kotowari.restful.data.ContextKey;
import kotowari.restful.data.Problem;
import kotowari.restful.data.RestContext;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.activity.behavior.RecordActivity;
import net.unit8.kysymys.activity.data.ActivityEvent;
import net.unit8.kysymys.user.data.UserId;
import net.unit8.raoh.Err;
import net.unit8.raoh.Ok;
import net.unit8.raoh.Result;
import org.jooq.DSLContext;
import tools.jackson.databind.JsonNode;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    public Problem validate(JsonNode body, RestContext context) {
        Result<ActivityJsonDecoders.RecordActivityInput> result =
                ActivityJsonDecoders.RECORD_ACTIVITY.decode(body);
        if (result instanceof Ok<ActivityJsonDecoders.RecordActivityInput> ok) {
            context.put(INPUT, ok.value());
            return null;
        }
        Err<ActivityJsonDecoders.RecordActivityInput> err =
                (Err<ActivityJsonDecoders.RecordActivityInput>) result;
        List<Problem.Violation> violations = err.issues().asList().stream()
                .map(i -> new Problem.Violation(i.path().toJsonPointer(), i.code(), i.message()))
                .toList();
        return Problem.fromViolationList(violations);
    }

    @Decision(POST)
    public boolean record(UserId caller, DSLContext dsl, RestContext context) {
        ActivityJsonDecoders.RecordActivityInput input = context.get(INPUT).orElseThrow();
        ActivityEvent event = new RecordActivity(dsl).apply(new RecordActivity.Input(
                caller, input.problemId(), input.kind(), input.detail(), LocalDateTime.now()));
        context.put(RECORDED, event);
        return true;
    }

    @Decision(HANDLE_CREATED)
    public Map<String, Object> handleCreated(RestContext context) {
        ActivityEvent event = context.get(RECORDED).orElseThrow();
        return Map.of(
                "id", event.id().value(),
                "kind", event.kind().name(),
                "recordedAt", event.occurredAt().toString());
    }
}
