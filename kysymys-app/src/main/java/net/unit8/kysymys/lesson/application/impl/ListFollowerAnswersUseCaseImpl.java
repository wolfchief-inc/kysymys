package net.unit8.kysymys.lesson.application.impl;

import net.unit8.kysymys.lesson.application.ListFollowerAnswersQuery;
import net.unit8.kysymys.lesson.application.ListFollowerAnswersUseCase;
import net.unit8.kysymys.lesson.application.ListAnswersPort;
import net.unit8.kysymys.lesson.domain.Answer;
import net.unit8.kysymys.stereotype.UseCase;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.data.domain.Page;

@UseCase
public class ListFollowerAnswersUseCaseImpl implements ListFollowerAnswersUseCase {
    private final ListAnswersPort listAnswersPort;

    public ListFollowerAnswersUseCaseImpl(ListAnswersPort listAnswersPort) {
        this.listAnswersPort = listAnswersPort;
    }

    @Override
    public Page<Answer> handle(ListFollowerAnswersQuery query) {
        UserId answererId = UserId.of(query.getUserId());
        return listAnswersPort.listRecentAnswers(answererId, 0, 5);
    }
}
