package net.unit8.kysymys.lesson.resource;

import kotowari.restful.Decision;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.lesson.dao.AnswerDao;
import net.unit8.kysymys.lesson.dao.SubmissionDao;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import static kotowari.restful.DecisionPoint.AUTHORIZED;
import static kotowari.restful.DecisionPoint.HANDLE_OK;

@AllowedMethods({"GET"})
public class MyAnswersResource {

    @Decision(AUTHORIZED)
    public boolean authorized(Principal principal) {
        return principal != null;
    }

    @Decision(HANDLE_OK)
    public List<Map<String, Object>> mine(UserId caller, DSLContext dsl) {
        AnswerDao answers = new AnswerDao(dsl);
        SubmissionDao submissions = new SubmissionDao(dsl);
        return answers.listByAnswerer(caller).stream()
                .map(a -> LessonJsonEncoders.encodeAnswer(a, submissions.findLatest(a.id())))
                .toList();
    }
}
