package net.unit8.kysymys.lesson.application;

import net.unit8.kysymys.lesson.domain.ProblemUpdatedEvent;

public interface UpdateProblemUseCase {
    ProblemUpdatedEvent handle(UpdateProblemCommand command);
}
