package net.unit8.kysymys.lesson.application;

import net.unit8.kysymys.lesson.domain.Answer;
import net.unit8.kysymys.lesson.domain.AnswerId;

import java.util.Optional;

public interface LoadAnswerPort {
    Optional<Answer> load(AnswerId answerId);
}
