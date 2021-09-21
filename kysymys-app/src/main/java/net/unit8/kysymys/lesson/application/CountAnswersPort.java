package net.unit8.kysymys.lesson.application;

import net.unit8.kysymys.lesson.domain.ProblemId;

public interface CountAnswersPort {
    long countByProblemId(ProblemId problemId);
}
