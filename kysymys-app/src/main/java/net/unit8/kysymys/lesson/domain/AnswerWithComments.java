package net.unit8.kysymys.lesson.domain;

import lombok.Value;
import org.springframework.data.domain.Page;

import java.util.List;

@Value
public class AnswerWithComments {
    Answer answer;
    List<Comment> comments;
}
