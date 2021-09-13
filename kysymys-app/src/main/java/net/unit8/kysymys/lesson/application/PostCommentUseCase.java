package net.unit8.kysymys.lesson.application;

import net.unit8.kysymys.lesson.domain.PostedCommentEvent;

public interface PostCommentUseCase {
    PostedCommentEvent handle(PostCommentCommand command);
}
