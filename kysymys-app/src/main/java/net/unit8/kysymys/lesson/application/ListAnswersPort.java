package net.unit8.kysymys.lesson.application;

import net.unit8.kysymys.lesson.domain.Answer;
import net.unit8.kysymys.lesson.domain.ProblemId;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ListAnswersPort {
    Page<Answer> listRecentAnswers(UserId answererId, int page, int size);
    List<Answer> listUserAnswersByProblemIds(UserId userId, List<ProblemId> problemIds);
}
