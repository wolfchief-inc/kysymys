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
import net.unit8.kysymys.lesson.data.AnswerId;
import net.unit8.kysymys.lesson.data.ReviewComment;
import net.unit8.kysymys.lesson.data.Submission;
import org.jooq.DSLContext;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static kotowari.restful.DecisionPoint.AUTHORIZED;
import static kotowari.restful.DecisionPoint.EXISTS;
import static kotowari.restful.DecisionPoint.HANDLE_OK;

@AllowedMethods({"GET"})
public class AnswerResource {

    static final ContextKey<Answer> ANSWER = ContextKey.of("answer", Answer.class);
    static final ContextKey<Submission> LATEST = ContextKey.of("latestSubmission", Submission.class);

    @Decision(AUTHORIZED)
    public boolean authorized(Principal principal) {
        return principal != null;
    }

    @Decision(EXISTS)
    public boolean exists(Parameters params, DSLContext dsl, RestContext context) {
        AnswerId id;
        try { id = new AnswerId(params.get("id")); }
        catch (IllegalArgumentException ex) { return false; }
        Optional<Answer> a = new AnswerDao(dsl).findById(id);
        if (a.isEmpty()) return false;
        Optional<Submission> latest = new SubmissionDao(dsl).findLatest(id);
        context.put(ANSWER, a.get());
        latest.ifPresent(s -> context.put(LATEST, s));
        return true;
    }

    @Decision(HANDLE_OK)
    public Map<String, Object> show(DSLContext dsl, RestContext context) {
        Answer a = context.get(ANSWER).orElseThrow();
        Optional<Submission> latest = context.get(LATEST);
        List<ReviewComment> comments = new ReviewCommentDao(dsl).listByAnswer(a.id());
        return AnswerJsonEncoders.encode(a, latest, comments);
    }
}
