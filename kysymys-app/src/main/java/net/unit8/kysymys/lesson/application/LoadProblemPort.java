package net.unit8.kysymys.lesson.application;

import net.unit8.kysymys.lesson.domain.Problem;
import net.unit8.kysymys.lesson.domain.ProblemId;

import java.util.Optional;

public interface LoadProblemPort {
    Optional<Problem> load(ProblemId problemId);
}
