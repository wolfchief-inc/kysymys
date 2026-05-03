package net.unit8.kysymys.lesson.resource;

import enkan.collection.Parameters;
import kotowari.restful.Decision;
import kotowari.restful.data.ContextKey;
import kotowari.restful.data.Problem;
import kotowari.restful.data.RestContext;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.lesson.behavior.ArchiveProblem;
import net.unit8.kysymys.lesson.behavior.UpdateProblem;
import net.unit8.kysymys.lesson.dao.ProblemDao;
import net.unit8.kysymys.lesson.data.ProblemId;
import net.unit8.kysymys.lesson.data.ProblemStatus;
import net.unit8.kysymys.user.data.UserId;
import net.unit8.raoh.Err;
import net.unit8.raoh.Ok;
import net.unit8.raoh.Result;
import org.jooq.DSLContext;
import tools.jackson.databind.JsonNode;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static kotowari.restful.DecisionPoint.ALLOWED;
import static kotowari.restful.DecisionPoint.AUTHORIZED;
import static kotowari.restful.DecisionPoint.DELETE;
import static kotowari.restful.DecisionPoint.EXISTS;
import static kotowari.restful.DecisionPoint.HANDLE_OK;
import static kotowari.restful.DecisionPoint.MALFORMED;
import static kotowari.restful.DecisionPoint.NEW;
import static kotowari.restful.DecisionPoint.PUT;
import static kotowari.restful.DecisionPoint.RESPOND_WITH_ENTITY;

@AllowedMethods({"GET", "PUT", "DELETE"})
public class ProblemResource {

    static final ContextKey<net.unit8.kysymys.lesson.data.Problem> PROBLEM =
            ContextKey.of("problem", net.unit8.kysymys.lesson.data.Problem.class);
    static final ContextKey<ProblemStatus> STATUS =
            ContextKey.of("status", ProblemStatus.class);
    static final ContextKey<ProblemJsonDecoders.UpdateInput> UPDATE_INPUT =
            ContextKey.of("updateInput", ProblemJsonDecoders.UpdateInput.class);

    @Decision(AUTHORIZED)
    public boolean authorized(Principal principal) {
        return principal != null;
    }

    @Decision(value = ALLOWED, method = {"PUT", "DELETE"})
    public boolean teacherOnly(Principal principal) {
        return principal instanceof enkan.security.bouncr.UserPermissionPrincipal p
                && p.permissions().contains("TEACHER");
    }

    @Decision(EXISTS)
    public boolean exists(Parameters params, DSLContext dsl, RestContext context) {
        ProblemId id;
        try {
            id = new ProblemId(params.get("id"));
        } catch (IllegalArgumentException ex) {
            return false;
        }
        Optional<net.unit8.kysymys.lesson.data.Problem> p = new ProblemDao(dsl).findById(id);
        if (p.isEmpty()) return false;
        ProblemStatus status = new ProblemDao(dsl).findStatus(p.get().lifecycleId())
                .orElse(ProblemStatus.ACTIVE);
        context.put(PROBLEM, p.get());
        context.put(STATUS, status);
        return true;
    }

    @Decision(value = MALFORMED, method = {"PUT"})
    public Problem validatePut(JsonNode body, RestContext context) {
        Result<ProblemJsonDecoders.UpdateInput> result = ProblemJsonDecoders.UPDATE.decode(body);
        if (result instanceof Ok<ProblemJsonDecoders.UpdateInput> ok) {
            context.put(UPDATE_INPUT, ok.value());
            return null;
        }
        Err<ProblemJsonDecoders.UpdateInput> err = (Err<ProblemJsonDecoders.UpdateInput>) result;
        return Problem.fromViolationList(ProblemsResource.toViolations(err));
    }

    @Decision(PUT)
    public boolean update(DSLContext dsl, UserId caller, RestContext context) {
        ProblemJsonDecoders.UpdateInput input = context.get(UPDATE_INPUT).orElseThrow();
        net.unit8.kysymys.lesson.data.Problem existing = context.get(PROBLEM).orElseThrow();
        Optional<net.unit8.kysymys.lesson.data.Problem> updated = new UpdateProblem(dsl).apply(
                new UpdateProblem.Input(existing.id(), input.name(), input.repository(),
                        caller, LocalDateTime.now()));
        updated.ifPresent(p -> context.put(PROBLEM, p));
        return updated.isPresent();
    }

    @Decision(DELETE)
    public boolean delete(DSLContext dsl, UserId caller, RestContext context) {
        net.unit8.kysymys.lesson.data.Problem existing = context.get(PROBLEM).orElseThrow();
        boolean ok = new ArchiveProblem(dsl).apply(
                new ArchiveProblem.Input(existing.id(), caller, LocalDateTime.now()));
        if (ok) {
            context.put(STATUS, ProblemStatus.ARCHIVED);
        }
        return ok;
    }

    @Decision(NEW)
    public boolean isNew() { return false; }   // PUT/DELETE return 200, not 201

    @Decision(RESPOND_WITH_ENTITY)
    public boolean respondWithEntity() { return true; }

    @Decision(HANDLE_OK)
    public Map<String, Object> show(RestContext context) {
        net.unit8.kysymys.lesson.data.Problem p = context.get(PROBLEM).orElseThrow();
        ProblemStatus s = context.get(STATUS).orElse(ProblemStatus.ACTIVE);
        return ProblemJsonEncoders.encode(p, s);
    }
}
