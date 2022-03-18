package net.unit8.kysymys.lesson.application;

import lombok.Value;

import java.time.LocalDateTime;

public interface PostCommentUseCase {
    PostedCommentEvent handle(PostCommentCommand command) throws AnswerNotFoundException;

    @Value
    class PostCommentCommand {
        String answerId;
        String commenterId;
        String description;
    }

    @Value
    class PostedCommentEvent {
        String answerId;
        String commenterId;
        LocalDateTime occurredAt;
    }
}
