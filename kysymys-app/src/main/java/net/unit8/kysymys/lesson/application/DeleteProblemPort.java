package net.unit8.kysymys.lesson.application;

import net.unit8.kysymys.lesson.domain.ProblemId;

public interface DeleteProblemPort {
    void delete(ProblemId problemId);
}
