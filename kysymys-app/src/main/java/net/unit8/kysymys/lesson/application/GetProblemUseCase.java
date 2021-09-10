package net.unit8.kysymys.lesson.application;

import net.unit8.kysymys.lesson.domain.Problem;

import java.util.Optional;

public interface GetProblemUseCase {
    Problem handle(String problemId);
}
