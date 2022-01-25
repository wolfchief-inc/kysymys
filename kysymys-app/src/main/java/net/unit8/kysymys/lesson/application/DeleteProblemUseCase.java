package net.unit8.kysymys.lesson.application;

import net.unit8.kysymys.lesson.domain.DeletedProblemEvent;

public interface DeleteProblemUseCase {
    DeletedProblemEvent handle(DeleteProblemCommand command) throws AlreadyHasAnswersException;
}
