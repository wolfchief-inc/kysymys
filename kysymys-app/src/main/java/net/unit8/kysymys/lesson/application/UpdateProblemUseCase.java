package net.unit8.kysymys.lesson.application;

public interface UpdateProblemUseCase {
    UpdatedProblemEvent handle(UpdateProblemCommand command);
}
