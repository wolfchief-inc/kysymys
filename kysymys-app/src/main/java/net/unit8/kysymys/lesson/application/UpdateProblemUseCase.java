package net.unit8.kysymys.lesson.application;

import lombok.Value;
import net.unit8.kysymys.lesson.domain.ProblemUpdatedEvent;

public interface UpdateProblemUseCase {
    ProblemUpdatedEvent handle(UpdateProblemCommand command);

    @Value
    class UpdateProblemCommand {
        String id;
        String name;
        String repositoryUrl;
        String branch;
        String readmePath;
        String updaterId;
    }
}
