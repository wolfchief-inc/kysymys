package net.unit8.kysymys.lesson.application;

import net.unit8.kysymys.lesson.domain.Answer;
import org.springframework.data.domain.Page;

public interface ListFollowerAnswersUseCase {
    Page<Answer> handle(ListFollowerAnswersQuery query);
}
