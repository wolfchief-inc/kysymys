package net.unit8.kysymys.lesson.application;

import lombok.Value;

import java.time.LocalDateTime;

public interface DeleteProblemUseCase {
    DeletedProblemEvent handle(DeleteProblemCommand command) throws AlreadyHasAnswersException;

    @Value
    class DeleteProblemCommand {
        String id;
        String deleterId;
    }

    @Value
    class DeletedProblemEvent {
        String problemId;
        String deleterId;
        LocalDateTime occurredAt;
    }
}
