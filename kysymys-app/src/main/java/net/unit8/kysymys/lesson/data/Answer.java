package net.unit8.kysymys.lesson.data;

import net.unit8.kysymys.user.data.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

public record Answer(
        AnswerId id,
        ProblemId problemId,
        UserId answererId,
        AnswerRepository repository,
        LocalDateTime lastAnsweredAt
) {
    public Answer {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(problemId, "problemId");
        Objects.requireNonNull(answererId, "answererId");
        Objects.requireNonNull(repository, "repository");
        Objects.requireNonNull(lastAnsweredAt, "lastAnsweredAt");
    }
}
