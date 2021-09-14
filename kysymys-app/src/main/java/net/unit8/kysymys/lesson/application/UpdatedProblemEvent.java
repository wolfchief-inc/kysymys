package net.unit8.kysymys.lesson.application;

import lombok.Value;
import net.unit8.kysymys.lesson.domain.ProblemId;

@Value
public class UpdatedProblemEvent {
    ProblemId problemId;
}
