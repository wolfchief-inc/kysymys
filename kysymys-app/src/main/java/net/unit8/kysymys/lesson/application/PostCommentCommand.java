package net.unit8.kysymys.lesson.application;

import lombok.Value;

@Value
public class PostCommentCommand {
    String answerId;
    String commenterId;
    String description;
}
