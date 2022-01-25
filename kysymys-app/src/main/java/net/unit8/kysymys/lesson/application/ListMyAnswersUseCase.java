package net.unit8.kysymys.lesson.application;

import lombok.Value;
import net.unit8.kysymys.lesson.domain.Answer;

import java.util.List;

public interface ListMyAnswersUseCase {
    List<Answer> handle(ListMyAnswersQuery query);

    @Value
    class ListMyAnswersQuery {
        String userId;
        List<String> problemIds;
    }
}
