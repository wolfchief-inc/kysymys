package net.unit8.kysymys.lesson.behavior;

import net.unit8.kysymys.events.SubmittedAnswerEvent;
import net.unit8.kysymys.lesson.dao.AnswerDao;
import net.unit8.kysymys.lesson.dao.ProblemDao;
import net.unit8.kysymys.lesson.dao.SubmissionDao;
import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.system.KysymysEventBus;
import net.unit8.kysymys.user.dao.ConnectionDao;
import net.unit8.kysymys.user.dao.UserDao;
import net.unit8.kysymys.user.data.User;
import net.unit8.kysymys.user.data.UserId;
import net.unit8.kysymys.user.data.UserName;
import org.jooq.DSLContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class SubmitAnswer {
    private final DSLContext dsl;
    private final KysymysEventBus eventBus;

    /** Test/legacy constructor: no event publication. */
    public SubmitAnswer(DSLContext dsl) {
        this(dsl, null);
    }

    public SubmitAnswer(DSLContext dsl, KysymysEventBus eventBus) {
        this.dsl = dsl;
        this.eventBus = eventBus;
    }

    public Optional<Output> apply(Input in) {
        // Confirm Problem exists outside the writing transaction so an unknown
        // problemId returns empty without leaving a partial answer.
        Optional<Problem> problem = new ProblemDao(dsl).findById(in.problemId());
        if (problem.isEmpty()) {
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

        if (eventBus != null) {
            // Look up answerer name + followers after the write commits so a publish
            // failure can never strand a half-written submission.
            UserName answererName = new UserDao(dsl).findById(in.answererId())
                    .map(User::name)
                    .orElse(new UserName(in.answererId().value()));
            List<UserId> followers = new ConnectionDao(dsl).listFollowersOf(in.answererId());
            eventBus.publish(new SubmittedAnswerEvent(
                    holder[0].answer().id(),
                    in.problemId(),
                    problem.get().name(),
                    in.answererId(),
                    answererName,
                    followers,
                    in.now()));
        }
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
