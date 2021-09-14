package net.unit8.kysymys.lesson.application;

import net.unit8.kysymys.lesson.domain.Answer;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.data.domain.Page;

public interface ListRecentAnswersPort {
    Page<Answer> listRecentAnswers(UserId answererId, int page, int size);
}
