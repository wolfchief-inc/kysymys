package net.unit8.kysymys.lesson.resource;

import kotowari.restful.Decision;
import kotowari.restful.data.ContextKey;
import kotowari.restful.data.Problem;
import kotowari.restful.data.RestContext;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.lesson.behavior.CreateProblem;
import net.unit8.kysymys.lesson.dao.ProblemDao;
import net.unit8.kysymys.lesson.data.ProblemStatus;
import net.unit8.kysymys.system.Problems;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;
import tools.jackson.databind.JsonNode;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import static kotowari.restful.DecisionPoint.ALLOWED;
import static kotowari.restful.DecisionPoint.AUTHORIZED;
import static kotowari.restful.DecisionPoint.HANDLE_CREATED;
import static kotowari.restful.DecisionPoint.HANDLE_OK;
import static kotowari.restful.DecisionPoint.MALFORMED;
import static kotowari.restful.DecisionPoint.POST;

@AllowedMethods({"GET", "POST"})
public class ProblemsResource {

    static final ContextKey<LessonJsonDecoders.CreateProblemInput> CREATE_INPUT =
            ContextKey.of("createInput", LessonJsonDecoders.CreateProblemInput.class);
    static final ContextKey<net.unit8.kysymys.lesson.data.Problem> CREATED_PROBLEM =
            ContextKey.of("createdProblem", net.unit8.kysymys.lesson.data.Problem.class);

    @Decision(AUTHORIZED)
    public boolean authorized(Principal principal) {
        return principal != null;
    }

    @Decision(value = ALLOWED, method = {"POST"})
    public boolean teacherOnly(Principal principal) {
        return principal instanceof enkan.security.bouncr.UserPermissionPrincipal p
                && p.permissions().contains("TEACHER");
    }

    @Decision(value = MALFORMED, method = {"POST"})
    public @Nullable Problem validatePost(JsonNode body, RestContext context) {
        return LessonJsonDecoders.CREATE_PROBLEM.decode(body).fold(
                input -> { context.put(CREATE_INPUT, input); return null; },
                Problems::of);
    }

    @Decision(POST)
    public boolean create(DSLContext dsl, UserId caller, RestContext context) {
        return context.get(CREATE_INPUT).map(input -> {
            context.put(CREATED_PROBLEM, new CreateProblem(dsl).apply(
                    new CreateProblem.Input(input.name(), input.repository(), caller, LocalDateTime.now())));
            return true;
        }).orElse(false);
    }

    @Decision(HANDLE_CREATED)
    public Map<String, Object> handleCreated(RestContext context) {
        return context.get(CREATED_PROBLEM)
                .map(p -> LessonJsonEncoders.encodeProblem(p, ProblemStatus.ACTIVE))
                .orElseThrow();
    }

    @Decision(HANDLE_OK)
    public List<Map<String, Object>> list(DSLContext dsl) {
        ProblemDao dao = new ProblemDao(dsl);
        return dao.listActive().stream()
                .map(p -> LessonJsonEncoders.encodeProblem(p, ProblemStatus.ACTIVE))
                .toList();
    }
}
