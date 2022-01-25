package net.unit8.kysymys.lesson.domain;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class DeletedProblemEvent {
    String problemId;
    String deleterId;
    LocalDateTime occurredAt;
}
