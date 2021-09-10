package net.unit8.kysymys.lesson.application.impl;

import net.unit8.kysymys.lesson.application.GetProblemUseCase;
import net.unit8.kysymys.lesson.application.LoadProblemPort;
import net.unit8.kysymys.lesson.application.ProblemNotFoundException;
import net.unit8.kysymys.lesson.domain.Problem;
import net.unit8.kysymys.lesson.domain.ProblemId;
import org.springframework.stereotype.Component;

@Component
class GetProblemUseCaseImpl implements GetProblemUseCase {
    private final LoadProblemPort loadProblemPort;

    public GetProblemUseCaseImpl(LoadProblemPort loadProblemPort) {
        this.loadProblemPort = loadProblemPort;
    }

    @Override
    public Problem handle(String problemId) {
        return loadProblemPort.load(ProblemId.of(problemId))
                .orElseThrow(() -> new ProblemNotFoundException(problemId));
    }
}
