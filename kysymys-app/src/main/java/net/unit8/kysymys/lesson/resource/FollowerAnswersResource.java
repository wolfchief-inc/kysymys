package net.unit8.kysymys.lesson.resource;

import kotowari.restful.Decision;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.lesson.dao.AnswerDao;
import net.unit8.kysymys.lesson.dao.SubmissionDao;
import net.unit8.kysymys.system.KysymysEventBus;
import net.unit8.kysymys.user.behavior.PrincipalRegistration;
import net.unit8.kysymys.user.dao.ConnectionDao;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import static kotowari.restful.DecisionPoint.AUTHORIZED;
import static kotowari.restful.DecisionPoint.HANDLE_OK;

/**
 * GET /followers/answers — answers submitted by users that the caller follows.
 * Sub-B deferred this; lives here in Sub-C because it needs the User
 * context's ConnectionDao.
 */
@AllowedMethods({"GET"})
public class FollowerAnswersResource {

    @Decision(AUTHORIZED)
    public boolean authorized(@Nullable Principal principal, DSLContext dsl, KysymysEventBus eventBus) {
        if (principal == null) return false;
        PrincipalRegistration.ensure(principal, dsl, eventBus);
        return true;
    }

    @Decision(HANDLE_OK)
    public List<Map<String, Object>> list(Principal principal, DSLContext dsl) {
        UserId caller = UserId.of(principal.getName());
        List<UserId> followees = new ConnectionDao(dsl).listFolloweesOf(caller);
        AnswerDao answers = new AnswerDao(dsl);
        SubmissionDao submissions = new SubmissionDao(dsl);
        return answers.listByAnswerers(followees).stream()
                .map(a -> LessonJsonEncoders.encodeAnswer(a, submissions.findLatest(a.id())))
                .toList();
    }
}
