package net.unit8.kysymys.lesson.application;

import net.unit8.kysymys.lesson.domain.Problem;

public interface SaveProblemPort {
    void save(Problem problem);
}
