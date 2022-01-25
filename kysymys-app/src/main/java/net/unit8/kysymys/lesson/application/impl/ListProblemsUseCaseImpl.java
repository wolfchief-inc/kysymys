package net.unit8.kysymys.lesson.application.impl;

import net.unit8.kysymys.lesson.application.GetProblemsPort;
import net.unit8.kysymys.lesson.application.ListProblemsUseCase;
import net.unit8.kysymys.lesson.domain.Problem;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
class ListProblemsUseCaseImpl implements ListProblemsUseCase {
    private final GetProblemsPort getProblemsPort;

    ListProblemsUseCaseImpl(GetProblemsPort getProblemsPort) {
        this.getProblemsPort = getProblemsPort;
    }

    @Override
    public Page<Problem> handle(ListProblemsQuery query) {
        return getProblemsPort.list(query.getPage());
    }
}
