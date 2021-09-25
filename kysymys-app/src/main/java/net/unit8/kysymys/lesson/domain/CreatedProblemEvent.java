package net.unit8.kysymys.lesson.domain;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class CreatedProblemEvent {
    String problemId;
    String creatorId;
    LocalDateTime occurredAt;
}
