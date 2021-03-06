package net.unit8.kysymys.lesson.domain;

import lombok.Value;
import net.unit8.kysymys.lesson.domain.ProblemId;

import java.io.Serializable;
import java.time.LocalDateTime;

@Value
public class ProblemUpdatedEvent implements Serializable {
    String problemId;
    String updaterId;
    LocalDateTime occurredAt;
}
