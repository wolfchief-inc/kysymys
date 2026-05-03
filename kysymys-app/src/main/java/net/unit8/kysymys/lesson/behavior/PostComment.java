package net.unit8.kysymys.lesson.behavior;

import net.unit8.kysymys.lesson.dao.AnswerDao;
import net.unit8.kysymys.lesson.dao.ReviewCommentDao;
import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;

import java.time.LocalDateTime;
import java.util.Optional;

public class PostComment {
    private final DSLContext dsl;

    public PostComment(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Optional<ReviewComment> apply(Input in) {
        if (new AnswerDao(dsl).findById(in.answerId()).isEmpty()) {
            return Optional.empty();
        }
        ReviewComment c = new ReviewComment(
                CommentId.newId(),
                in.answerId(),
                in.commenterId(),
                in.description(),
                in.now());
        dsl.transaction(cfg -> new ReviewCommentDao(cfg.dsl()).insert(c));
        return Optional.of(c);
    }

    public record Input(
            AnswerId answerId,
            UserId commenterId,
            Description description,
            LocalDateTime now
    ) {}
}
