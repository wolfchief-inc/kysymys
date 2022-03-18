package net.unit8.kysymys.lesson.application;

import lombok.Value;

import java.time.LocalDateTime;

public interface CreateProblemUseCase {
    CreatedProblemEvent handle(CreateProblemCommand command);

    @Value
    class CreateProblemCommand {
        String name;
        String repositoryUrl;
        String branch;
        String readmePath;
        String creatorId;
    }

    @Value
    class CreatedProblemEvent {
        String problemId;
        String creatorId;
        LocalDateTime occurredAt;
    }

}
