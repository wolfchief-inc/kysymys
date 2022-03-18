package net.unit8.kysymys.lesson.application;

import lombok.Value;
import net.unit8.kysymys.lesson.domain.Answer;
import net.unit8.kysymys.lesson.domain.Comment;

import java.util.List;

@Value
public class AnswerWithComments {
    Answer answer;
    List<Comment> comments;
}
