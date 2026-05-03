package net.unit8.kysymys.lesson.behavior;

import net.unit8.kysymys.lesson.dao.AnswerDao;
import net.unit8.kysymys.lesson.dao.ProblemDao;
import net.unit8.kysymys.lesson.dao.SubmissionDao;
import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;

import java.time.LocalDateTime;
import java.util.Optional;

public class SubmitAnswer {
    private final DSLContext dsl;

    public SubmitAnswer(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Optional<Output> apply(Input in) {
        // Confirm Problem exists outside the writing transaction so an unknown
        // problemId returns empty without leaving a partial answer.
        if (new ProblemDao(dsl).findById(in.problemId()).isEmpty()) {
            return Optional.empty();
        }

        Output[] holder = new Output[1];
        dsl.transaction(cfg -> {
            DSLContext tx = cfg.dsl();
            AnswerId aid = new AnswerDao(tx).upsert(
                    in.problemId(), in.answererId(), in.repository(), in.now());
            Submission submission = new Submission(
                    SubmissionId.newId(), aid, in.commitHash(), in.now());
            new SubmissionDao(tx).insertAndMarkLatest(submission);

            Answer ans = new Answer(
                    aid, in.problemId(), in.answererId(), in.repository(), in.now());
            holder[0] = new Output(ans, submission);
        });
        return Optional.of(holder[0]);
    }

    public record Input(
            ProblemId problemId,
            UserId answererId,
            AnswerRepository repository,
            CommitHash commitHash,
            LocalDateTime now
    ) {}

    public record Output(Answer answer, Submission submission) {}
}
