package net.unit8.kysymys.lesson.resource;

import enkan.collection.Parameters;
import kotowari.restful.Decision;
import kotowari.restful.data.ContextKey;
import kotowari.restful.data.RestContext;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.lesson.dao.AnswerDao;
import net.unit8.kysymys.lesson.dao.ReviewCommentDao;
import net.unit8.kysymys.lesson.dao.SubmissionDao;
import net.unit8.kysymys.lesson.data.Answer;
import net.unit8.kysymys.system.RestContexts;
import org.jooq.DSLContext;

import java.security.Principal;
import java.util.Map;

import static kotowari.restful.DecisionPoint.AUTHORIZED;
import static kotowari.restful.DecisionPoint.EXISTS;
import static kotowari.restful.DecisionPoint.HANDLE_OK;

@AllowedMethods({"GET"})
public class AnswerResource {

    static final ContextKey<Answer> ANSWER = ContextKey.of("answer", Answer.class);

    @Decision(AUTHORIZED)
    public boolean authorized(Principal principal) {
        return principal != null;
    }

    @Decision(EXISTS)
    public boolean exists(Parameters params, DSLContext dsl, RestContext context) {
        return LessonPathDecoders.ANSWER_ID.decode(params.get("id"))
                .fold(id -> RestContexts.stash(context, ANSWER, new AnswerDao(dsl).findById(id)), _ -> false);
    }

    @Decision(HANDLE_OK)
    public Map<String, Object> show(DSLContext dsl, RestContext context) {
        return context.get(ANSWER)
                .map(answer -> LessonJsonEncoders.encodeAnswer(
                        answer,
                        new SubmissionDao(dsl).findLatest(answer.id()),
                        new ReviewCommentDao(dsl).listByAnswer(answer.id())))
                .orElseThrow();
    }
}
