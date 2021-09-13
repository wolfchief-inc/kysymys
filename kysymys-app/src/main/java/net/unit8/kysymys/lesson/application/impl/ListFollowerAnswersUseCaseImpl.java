package net.unit8.kysymys.lesson.application.impl;

import net.unit8.kysymys.lesson.application.ListFollowerAnswersQuery;
import net.unit8.kysymys.lesson.application.ListFollowerAnswersUseCase;
import net.unit8.kysymys.lesson.domain.Answer;
import org.springframework.data.domain.Page;

public class ListFollowerAnswersUseCaseImpl implements ListFollowerAnswersUseCase {
    @Override
    public Page<Answer> handle(ListFollowerAnswersQuery query) {
        return null;
    }
}
