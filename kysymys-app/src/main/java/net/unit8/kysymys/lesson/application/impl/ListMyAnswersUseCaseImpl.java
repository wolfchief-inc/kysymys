package net.unit8.kysymys.lesson.application.impl;

import net.unit8.kysymys.lesson.application.ListAnswersPort;
import net.unit8.kysymys.lesson.application.ListMyAnswersUseCase;
import net.unit8.kysymys.lesson.domain.Answer;
import net.unit8.kysymys.lesson.domain.ProblemId;
import net.unit8.kysymys.stereotype.UseCase;
import net.unit8.kysymys.user.domain.UserId;

import java.util.List;
import java.util.stream.Collectors;

@UseCase
public class ListMyAnswersUseCaseImpl implements ListMyAnswersUseCase {
    private final ListAnswersPort listAnswersPort;

    public ListMyAnswersUseCaseImpl(ListAnswersPort listAnswersPort) {
        this.listAnswersPort = listAnswersPort;
    }

    @Override
    public List<Answer> handle(ListMyAnswersQuery query) {
        UserId answererId = UserId.of(query.getUserId());
        return listAnswersPort.listUserAnswersByProblemIds(answererId, query.getProblemIds().stream().map(ProblemId::of).collect(Collectors.toList()));
    }
}
