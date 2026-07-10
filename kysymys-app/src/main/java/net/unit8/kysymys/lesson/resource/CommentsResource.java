package net.unit8.kysymys.lesson.resource;

import enkan.collection.Parameters;
import kotowari.restful.Decision;
import kotowari.restful.data.ContextKey;
import kotowari.restful.data.Problem;
import kotowari.restful.data.RestContext;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.lesson.behavior.PostComment;
import net.unit8.kysymys.lesson.data.ReviewComment;
import net.unit8.kysymys.system.Problems;
import net.unit8.kysymys.system.RestContexts;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;
import tools.jackson.databind.JsonNode;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

import static kotowari.restful.DecisionPoint.AUTHORIZED;
import static kotowari.restful.DecisionPoint.HANDLE_CREATED;
import static kotowari.restful.DecisionPoint.MALFORMED;
import static kotowari.restful.DecisionPoint.POST;

@AllowedMethods({"POST"})
public class CommentsResource {

    static final ContextKey<LessonJsonDecoders.PostCommentInput> POST_INPUT =
            ContextKey.of("commentInput", LessonJsonDecoders.PostCommentInput.class);
    static final ContextKey<ReviewComment> CREATED =
            ContextKey.of("createdComment", ReviewComment.class);

    @Decision(AUTHORIZED)
    public boolean authorized(Principal principal) {
        return principal != null;
    }

    @Decision(value = MALFORMED, method = {"POST"})
    public Problem validate(JsonNode body, RestContext context) {
        return LessonJsonDecoders.POST_COMMENT.decode(body).fold(
                input -> { context.put(POST_INPUT, input); return null; },
                Problems::of);
    }

    @Decision(POST)
    public boolean create(Parameters params, DSLContext dsl, UserId caller, RestContext context) {
        return context.get(POST_INPUT).map(input ->
                LessonPathDecoders.ANSWER_ID.decode(params.get("id")).fold(
                        aid -> RestContexts.stash(context, CREATED, new PostComment(dsl).apply(
                                new PostComment.Input(aid, caller, input.description(), LocalDateTime.now()))),
                        _ -> false))
                .orElse(false);
    }

    @Decision(HANDLE_CREATED)
    public Map<String, Object> handleCreated(RestContext context) {
        return context.get(CREATED).map(LessonJsonEncoders::encodeComment).orElseThrow();
    }
}
