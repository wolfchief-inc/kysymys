package net.unit8.kysymys.lesson.domain;

import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

@Value
public class PostedCommentEvent implements Serializable {
    String answerId;
    String commenterId;
    LocalDateTime occurredAt;
}
