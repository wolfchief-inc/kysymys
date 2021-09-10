package net.unit8.kysymys.lesson.application;

import net.unit8.kysymys.lesson.domain.Problem;
import org.springframework.data.domain.Page;

public interface GetProblemsPort {
    Page<Problem> list(int page);
}
