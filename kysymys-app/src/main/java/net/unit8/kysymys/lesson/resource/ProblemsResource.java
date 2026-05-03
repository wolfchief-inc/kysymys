package net.unit8.kysymys.lesson.resource;

import kotowari.restful.Decision;
import kotowari.restful.data.ContextKey;
import kotowari.restful.data.Problem;
import kotowari.restful.data.RestContext;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.lesson.behavior.CreateProblem;
import net.unit8.kysymys.lesson.dao.ProblemDao;
import net.unit8.kysymys.lesson.data.ProblemStatus;
import net.unit8.kysymys.user.data.UserId;
import net.unit8.raoh.Err;
import net.unit8.raoh.Issue;
import net.unit8.raoh.Ok;
import net.unit8.raoh.Result;
import org.jooq.DSLContext;
import tools.jackson.databind.JsonNode;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static kotowari.restful.DecisionPoint.ALLOWED;
import static kotowari.restful.DecisionPoint.AUTHORIZED;
import static kotowari.restful.DecisionPoint.HANDLE_CREATED;
import static kotowari.restful.DecisionPoint.HANDLE_OK;
import static kotowari.restful.DecisionPoint.MALFORMED;
import static kotowari.restful.DecisionPoint.POST;

@AllowedMethods({"GET", "POST"})
public class ProblemsResource {

    static final ContextKey<ProblemJsonDecoders.CreateInput> CREATE_INPUT =
            ContextKey.of("createInput", ProblemJsonDecoders.CreateInput.class);
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
    public Problem validatePost(JsonNode body, RestContext context) {
        Result<ProblemJsonDecoders.CreateInput> result = ProblemJsonDecoders.CREATE.decode(body);
        if (result instanceof Ok<ProblemJsonDecoders.CreateInput> ok) {
            context.put(CREATE_INPUT, ok.value());
            return null;
        }
        Err<ProblemJsonDecoders.CreateInput> err = (Err<ProblemJsonDecoders.CreateInput>) result;
        return Problem.fromViolationList(toViolations(err));
    }

    @Decision(POST)
    public boolean create(DSLContext dsl, UserId caller, RestContext context) {
        ProblemJsonDecoders.CreateInput input = context.get(CREATE_INPUT).orElseThrow();
        net.unit8.kysymys.lesson.data.Problem created = new CreateProblem(dsl).apply(
                new CreateProblem.Input(input.name(), input.repository(), caller, LocalDateTime.now()));
        context.put(CREATED_PROBLEM, created);
        return true;
    }

    @Decision(HANDLE_CREATED)
    public Map<String, Object> handleCreated(RestContext context) {
        net.unit8.kysymys.lesson.data.Problem p = context.get(CREATED_PROBLEM).orElseThrow();
        return ProblemJsonEncoders.encode(p, ProblemStatus.ACTIVE);
    }

    @Decision(HANDLE_OK)
    public List<Map<String, Object>> list(DSLContext dsl) {
        ProblemDao dao = new ProblemDao(dsl);
        return dao.listActive().stream()
                .map(p -> ProblemJsonEncoders.encode(p, ProblemStatus.ACTIVE))
                .toList();
    }

    static <T> List<Problem.Violation> toViolations(Err<T> err) {
        return err.issues().asList().stream()
                .map(ProblemsResource::toViolation)
                .toList();
    }

    private static Problem.Violation toViolation(Issue issue) {
        return new Problem.Violation(
                issue.path().toJsonPointer(), issue.code(), issue.message());
    }
}
