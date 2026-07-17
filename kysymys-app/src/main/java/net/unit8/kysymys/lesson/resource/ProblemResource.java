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
import net.unit8.kysymys.lesson.data.ProblemStatus;
import net.unit8.kysymys.system.Optionals;
import net.unit8.kysymys.system.Problems;
import net.unit8.kysymys.system.RestContexts;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;
import tools.jackson.databind.JsonNode;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

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
    static final ContextKey<LessonJsonDecoders.UpdateProblemInput> UPDATE_INPUT =
            ContextKey.of("updateInput", LessonJsonDecoders.UpdateProblemInput.class);

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
        return LessonPathDecoders.PROBLEM_ID.decode(params.get("id"))
                .fold(id -> stashProblem(context, dsl, new ProblemDao(dsl).findById(id)), _ -> false);
    }

    /** Remembers the problem together with its derived lifecycle status, or reports absence. */
    private static boolean stashProblem(RestContext context, DSLContext dsl,
                                        Optional<net.unit8.kysymys.lesson.data.Problem> found) {
        found.ifPresent(problem -> {
            context.put(PROBLEM, problem);
            context.put(STATUS, new ProblemDao(dsl).findStatus(problem.lifecycleId())
                    .orElse(ProblemStatus.ACTIVE));
        });
        return found.isPresent();
    }

    @Decision(value = MALFORMED, method = {"PUT"})
    public @Nullable Problem validatePut(JsonNode body, RestContext context) {
        return LessonJsonDecoders.UPDATE_PROBLEM.decode(body).fold(
                input -> { context.put(UPDATE_INPUT, input); return null; },
                Problems::of);
    }

    @Decision(PUT)
    public boolean update(DSLContext dsl, UserId caller, RestContext context) {
        return Optionals.map2(context.get(UPDATE_INPUT), context.get(PROBLEM),
                        (input, existing) -> RestContexts.stash(context, PROBLEM, new UpdateProblem(dsl).apply(
                                new UpdateProblem.Input(existing.id(), input.name(), input.repository(),
                                        caller, LocalDateTime.now()))))
                .orElse(false);
    }

    @Decision(DELETE)
    public boolean delete(DSLContext dsl, UserId caller, RestContext context) {
        return context.get(PROBLEM)
                .map(existing -> {
                    boolean archived = new ArchiveProblem(dsl).apply(
                            new ArchiveProblem.Input(existing.id(), caller, LocalDateTime.now()));
                    if (archived) {
                        context.put(STATUS, ProblemStatus.ARCHIVED);
                    }
                    return archived;
                })
                .orElse(false);
    }

    @Decision(NEW)
    public boolean isNew() { return false; }   // PUT/DELETE return 200, not 201

    @Decision(RESPOND_WITH_ENTITY)
    public boolean respondWithEntity() { return true; }

    @Decision(HANDLE_OK)
    public Map<String, Object> show(RestContext context) {
        return context.get(PROBLEM)
                .map(problem -> LessonJsonEncoders.encodeProblem(
                        problem, context.get(STATUS).orElse(ProblemStatus.ACTIVE)))
                .orElseThrow();
    }
}
