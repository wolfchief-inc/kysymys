package net.unit8.kysymys.events;

import net.unit8.kysymys.lesson.data.AnswerId;
import net.unit8.kysymys.lesson.data.ProblemId;
import net.unit8.kysymys.lesson.data.ProblemName;
import net.unit8.kysymys.user.data.UserId;
import net.unit8.kysymys.user.data.UserName;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Published by Lesson's SubmitAnswer behavior after a successful submission.
 * Notification subscribes to fan out WhatsNew rows to followers.
 *
 * <p>{@code followers} is a snapshot of the answerer's followers captured at
 * the time of submission, looked up via {@code ConnectionDao.listFollowersOf}.
 */
public record SubmittedAnswerEvent(
        AnswerId answerId,
        ProblemId problemId,
        ProblemName problemName,
        UserId answererId,
        UserName answererName,
        List<UserId> followers,
        LocalDateTime occurredAt
) implements KysymysEvent {
    public SubmittedAnswerEvent {
        Objects.requireNonNull(answerId, "answerId");
        Objects.requireNonNull(problemId, "problemId");
        Objects.requireNonNull(problemName, "problemName");
        Objects.requireNonNull(answererId, "answererId");
        Objects.requireNonNull(answererName, "answererName");
        followers = followers == null ? List.of() : List.copyOf(followers);
        Objects.requireNonNull(occurredAt, "occurredAt");
    }
}
