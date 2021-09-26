package net.unit8.kysymys.lesson.domain;

import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

@Value
public class CreatedProblemEvent implements Serializable {
    String problemId;
    String creatorId;
    LocalDateTime occurredAt;
}
