package net.unit8.kysymys.lesson.resource;

import enkan.collection.Parameters;
import kotowari.restful.Decision;
import kotowari.restful.data.ContextKey;
import kotowari.restful.data.Problem;
import kotowari.restful.data.RestContext;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.lesson.behavior.PostComment;
import net.unit8.kysymys.lesson.data.AnswerId;
import net.unit8.kysymys.lesson.data.ReviewComment;
import net.unit8.kysymys.user.data.UserId;
import net.unit8.raoh.Err;
import net.unit8.raoh.Ok;
import net.unit8.raoh.Result;
import org.jooq.DSLContext;
import tools.jackson.databind.JsonNode;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static kotowari.restful.DecisionPoint.AUTHORIZED;
import static kotowari.restful.DecisionPoint.HANDLE_CREATED;
import static kotowari.restful.DecisionPoint.MALFORMED;
import static kotowari.restful.DecisionPoint.POST;

@AllowedMethods({"POST"})
public class CommentsResource {

    static final ContextKey<CommentJsonDecoders.PostInput> POST_INPUT =
            ContextKey.of("commentInput", CommentJsonDecoders.PostInput.class);
    static final ContextKey<ReviewComment> CREATED =
            ContextKey.of("createdComment", ReviewComment.class);

    @Decision(AUTHORIZED)
    public boolean authorized(Principal principal) {
        return principal != null;
    }

    @Decision(value = MALFORMED, method = {"POST"})
    public Problem validate(JsonNode body, RestContext context) {
        Result<CommentJsonDecoders.PostInput> result = CommentJsonDecoders.POST.decode(body);
        if (result instanceof Ok<CommentJsonDecoders.PostInput> ok) {
            context.put(POST_INPUT, ok.value());
            return null;
        }
        Err<CommentJsonDecoders.PostInput> err = (Err<CommentJsonDecoders.PostInput>) result;
        return Problem.fromViolationList(ProblemsResource.toViolations(err));
    }

    @Decision(POST)
    public boolean create(Parameters params, DSLContext dsl, UserId caller, RestContext context) {
        CommentJsonDecoders.PostInput input = context.get(POST_INPUT).orElseThrow();
        AnswerId aid;
        try { aid = new AnswerId(params.get("id")); }
        catch (IllegalArgumentException ex) { return false; }

        Optional<ReviewComment> created = new PostComment(dsl).apply(
                new PostComment.Input(aid, caller, input.description(), LocalDateTime.now()));
        created.ifPresent(c -> context.put(CREATED, c));
        return created.isPresent();
    }

    @Decision(HANDLE_CREATED)
    public Map<String, Object> handleCreated(RestContext context) {
        ReviewComment c = context.get(CREATED).orElseThrow();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", c.id().value());
        body.put("answerId", c.answerId().value());
        body.put("commenterId", c.commenterId().value());
        body.put("description", c.description().value());
        body.put("postedAt", c.postedAt().toString());
        return body;
    }
}
