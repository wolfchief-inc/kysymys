package net.unit8.kysymys.lesson.resource;

import enkan.collection.Parameters;
import kotowari.restful.Decision;
import kotowari.restful.data.ContextKey;
import kotowari.restful.data.Problem;
import kotowari.restful.data.RestContext;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.lesson.behavior.SubmitAnswer;
import net.unit8.kysymys.system.KysymysEventBus;
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

import static kotowari.restful.DecisionPoint.AUTHORIZED;
import static kotowari.restful.DecisionPoint.HANDLE_CREATED;
import static kotowari.restful.DecisionPoint.MALFORMED;
import static kotowari.restful.DecisionPoint.POST;

@AllowedMethods({"POST"})
public class AnswersResource {

    static final ContextKey<LessonJsonDecoders.SubmitAnswerInput> SUBMIT_INPUT =
            ContextKey.of("submitInput", LessonJsonDecoders.SubmitAnswerInput.class);
    static final ContextKey<SubmitAnswer.Output> OUTPUT =
            ContextKey.of("submitOutput", SubmitAnswer.Output.class);

    @Decision(AUTHORIZED)
    public boolean authorized(Principal principal) {
        return principal != null;
    }

    @Decision(value = MALFORMED, method = {"POST"})
    public @Nullable Problem validate(JsonNode body, RestContext context) {
        return LessonJsonDecoders.SUBMIT_ANSWER.decode(body).fold(
                input -> { context.put(SUBMIT_INPUT, input); return null; },
                Problems::of);
    }

    @Decision(POST)
    public boolean submit(Parameters params, DSLContext dsl, UserId caller,
                          KysymysEventBus eventBus, RestContext context) {
        return context.get(SUBMIT_INPUT).map(input ->
                LessonPathDecoders.PROBLEM_ID.decode(params.get("id")).fold(
                        pid -> RestContexts.stash(context, OUTPUT, new SubmitAnswer(dsl, eventBus).apply(
                                new SubmitAnswer.Input(pid, caller, input.repository(), input.commitHash(),
                                        LocalDateTime.now()))),
                        _ -> false))
                .orElse(false);
    }

    @Decision(HANDLE_CREATED)
    public Map<String, Object> handleCreated(RestContext context) {
        return context.get(OUTPUT)
                .map(out -> LessonJsonEncoders.encodeAnswer(out.answer(), Optional.of(out.submission())))
                .orElseThrow();
    }
}
