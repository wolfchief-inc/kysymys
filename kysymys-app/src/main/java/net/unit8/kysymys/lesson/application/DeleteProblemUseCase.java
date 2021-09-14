package net.unit8.kysymys.lesson.application;

public interface DeleteProblemUseCase {
    DeletedProblemEvent handle(DeleteProblemCommand command);
}
