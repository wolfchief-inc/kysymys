package net.unit8.kysymys.lesson.application;

import net.unit8.kysymys.lesson.domain.Answer;

import java.util.List;

public interface ListMyAnswersUseCase {
    List<Answer> handle(ListMyAnswersQuery query);
}
