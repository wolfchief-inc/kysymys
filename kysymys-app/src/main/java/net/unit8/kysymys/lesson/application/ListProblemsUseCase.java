package net.unit8.kysymys.lesson.application;

import lombok.Value;
import net.unit8.kysymys.lesson.domain.Problem;
import org.springframework.data.domain.Page;

public interface ListProblemsUseCase {
    Page<Problem> handle(ListProblemsQuery query);

    @Value
    class ListProblemsQuery {
        int page;
        int size;
    }
}
