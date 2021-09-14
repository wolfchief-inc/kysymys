package net.unit8.kysymys.lesson.application;

import net.unit8.kysymys.lesson.domain.AnswerWithComments;

public interface ShowAnswerUseCase {
    AnswerWithComments handle(ShowAnswerQuery query);
}
