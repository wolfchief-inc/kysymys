package net.unit8.kysymys.lesson.application;

import net.unit8.kysymys.lesson.domain.CreatedProblemEvent;

public interface CreateProblemUseCase {
    CreatedProblemEvent handle(CreateProblemCommand command);
}
