package net.unit8.kysymys.lesson.application;

import lombok.Value;

@Value
public class ShowAnswerQuery {
    String answerId;
    String viewerUserId;
}
