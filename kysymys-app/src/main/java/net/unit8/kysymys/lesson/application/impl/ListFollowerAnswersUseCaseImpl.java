package net.unit8.kysymys.lesson.application.impl;

import net.unit8.kysymys.lesson.application.ListFollowerAnswersQuery;
import net.unit8.kysymys.lesson.application.ListFollowerAnswersUseCase;
import net.unit8.kysymys.lesson.application.ListRecentAnswersPort;
import net.unit8.kysymys.lesson.domain.Answer;
import net.unit8.kysymys.stereotype.UseCase;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.data.domain.Page;

@UseCase
public class ListFollowerAnswersUseCaseImpl implements ListFollowerAnswersUseCase {
    private final ListRecentAnswersPort listRecentAnswersPort;

    public ListFollowerAnswersUseCaseImpl(ListRecentAnswersPort listRecentAnswersPort) {
        this.listRecentAnswersPort = listRecentAnswersPort;
    }

    @Override
    public Page<Answer> handle(ListFollowerAnswersQuery query) {
        UserId answererId = UserId.of(query.getUserId());
        return listRecentAnswersPort.listRecentAnswers(answererId, 0, 5);
    }
}
