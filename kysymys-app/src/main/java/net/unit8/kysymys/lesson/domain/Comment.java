package net.unit8.kysymys.lesson.domain;

import am.ik.yavi.arguments.*;
import am.ik.yavi.builder.ArgumentsValidatorBuilder;
import lombok.Value;
import net.unit8.kysymys.user.domain.UserId;

import java.time.LocalDateTime;

@Value
public class Comment {
    public static Arguments5Validator<CommentId, Answer, UserId, Description, LocalDateTime, Comment> validator = ArgumentsValidatorBuilder.of(Comment::new)
            .builder(b -> b._object(Arguments1::arg1, "id", c -> c.notNull()))
            .builder(b -> b._object(Arguments2::arg2, "answer", c -> c.notNull()))
            .builder(b -> b._object(Arguments3::arg3, "commenterId", c -> c.notNull()))
            .builder(b -> b._object(Arguments4::arg4, "description", c -> c.notNull()))
            .builder(b -> b._object(Arguments5::arg5, "postedAt", c -> c.notNull()))
            .build();

    CommentId id;
    Answer answer;
    UserId commenterId;
    Description description;
    LocalDateTime postedAt;

    public static Comment of(CommentId id, Answer answer, UserId commenterId, Description description, LocalDateTime postedAt) {
        return validator.validated(id, answer, commenterId, description, postedAt);
    }
}
