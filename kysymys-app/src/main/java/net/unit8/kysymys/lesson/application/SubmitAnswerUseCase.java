package net.unit8.kysymys.lesson.application;

import net.unit8.kysymys.lesson.domain.SubmittedAnswerEvent;

public interface SubmitAnswerUseCase {
    SubmittedAnswerEvent handle(SubmitAnswerCommand command);
}
