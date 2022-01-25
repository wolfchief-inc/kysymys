package net.unit8.kysymys.lesson.application;

import lombok.Value;
import net.unit8.kysymys.lesson.domain.PostedCommentEvent;

public interface PostCommentUseCase {
    PostedCommentEvent handle(PostCommentCommand command) throws AnswerNotFoundException;

    @Value
    class PostCommentCommand {
        String answerId;
        String commenterId;
        String description;
    }
}
