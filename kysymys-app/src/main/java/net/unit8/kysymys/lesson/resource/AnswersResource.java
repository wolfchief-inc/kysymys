package net.unit8.kysymys.lesson.resource;

import enkan.collection.Parameters;
import kotowari.restful.Decision;
import kotowari.restful.data.ContextKey;
import kotowari.restful.data.Problem;
import kotowari.restful.data.RestContext;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.lesson.behavior.SubmitAnswer;
import net.unit8.kysymys.lesson.data.ProblemId;
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

import static kotowari.restful.DecisionPoint.AUTHORIZED;
import static kotowari.restful.DecisionPoint.HANDLE_CREATED;
import static kotowari.restful.DecisionPoint.MALFORMED;
import static kotowari.restful.DecisionPoint.POST;

@AllowedMethods({"POST"})
public class AnswersResource {

    static final ContextKey<AnswerJsonDecoders.SubmitInput> SUBMIT_INPUT =
            ContextKey.of("submitInput", AnswerJsonDecoders.SubmitInput.class);
    static final ContextKey<SubmitAnswer.Output> OUTPUT =
            ContextKey.of("submitOutput", SubmitAnswer.Output.class);

    @Decision(AUTHORIZED)
    public boolean authorized(Principal principal) {
        return principal != null;
    }

    @Decision(value = MALFORMED, method = {"POST"})
    public Problem validate(JsonNode body, RestContext context) {
        Result<AnswerJsonDecoders.SubmitInput> result = AnswerJsonDecoders.SUBMIT.decode(body);
        if (result instanceof Ok<AnswerJsonDecoders.SubmitInput> ok) {
            context.put(SUBMIT_INPUT, ok.value());
            return null;
        }
        Err<AnswerJsonDecoders.SubmitInput> err = (Err<AnswerJsonDecoders.SubmitInput>) result;
        return Problem.fromViolationList(ProblemsResource.toViolations(err));
    }

    @Decision(POST)
    public boolean submit(Parameters params, DSLContext dsl, UserId caller, RestContext context) {
        AnswerJsonDecoders.SubmitInput input = context.get(SUBMIT_INPUT).orElseThrow();
        ProblemId pid;
        try { pid = new ProblemId(params.get("id")); }
        catch (IllegalArgumentException ex) { return false; }

        Optional<SubmitAnswer.Output> out = new SubmitAnswer(dsl).apply(
                new SubmitAnswer.Input(
                        pid, caller, input.repository(), input.commitHash(),
                        LocalDateTime.now()));
        out.ifPresent(o -> context.put(OUTPUT, o));
        return out.isPresent();
    }

    @Decision(HANDLE_CREATED)
    public Map<String, Object> handleCreated(RestContext context) {
        SubmitAnswer.Output out = context.get(OUTPUT).orElseThrow();
        return AnswerJsonEncoders.encode(out.answer(), Optional.of(out.submission()));
    }
}
