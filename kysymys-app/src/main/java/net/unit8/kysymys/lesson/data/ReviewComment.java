package net.unit8.kysymys.lesson.data;

import net.unit8.kysymys.user.data.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

public record ReviewComment(
        CommentId id,
        AnswerId answerId,
        UserId commenterId,
        Description description,
        LocalDateTime postedAt
) {
    public ReviewComment {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(answerId, "answerId");
        Objects.requireNonNull(commenterId, "commenterId");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(postedAt, "postedAt");
    }
}
