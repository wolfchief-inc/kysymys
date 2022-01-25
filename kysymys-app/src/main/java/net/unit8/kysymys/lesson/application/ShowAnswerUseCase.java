package net.unit8.kysymys.lesson.application;

import lombok.Value;
import net.unit8.kysymys.lesson.domain.AnswerWithComments;

public interface ShowAnswerUseCase {
    AnswerWithComments handle(ShowAnswerQuery query) throws AnswerNotFoundException;

    @Value
    class ShowAnswerQuery {
        String answerId;
        String viewerUserId;
    }
}
