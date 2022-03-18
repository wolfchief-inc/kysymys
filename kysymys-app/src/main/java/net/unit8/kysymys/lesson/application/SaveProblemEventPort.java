package net.unit8.kysymys.lesson.application;

import net.unit8.kysymys.lesson.application.CreateProblemUseCase.CreatedProblemEvent;

public interface SaveProblemEventPort {
    void save(CreatedProblemEvent event);
}
