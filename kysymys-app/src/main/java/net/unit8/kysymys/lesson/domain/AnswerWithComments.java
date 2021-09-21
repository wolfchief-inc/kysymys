package net.unit8.kysymys.lesson.domain;

import lombok.Value;

import java.util.List;

@Value
public class AnswerWithComments {
    Answer answer;
    List<Comment> comments;
}
