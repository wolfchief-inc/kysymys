package net.unit8.kysymys.lesson.domain;

import lombok.Value;

@Value
public class CreatedProblemEvent {
    String problemId;
    String creatorId;
}
