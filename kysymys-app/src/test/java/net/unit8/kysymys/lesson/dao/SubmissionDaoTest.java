package net.unit8.kysymys.lesson.dao;

import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SubmissionDaoTest {
    private static DaoTestSupport support;
    private static AnswerDao answers;
    private static SubmissionDao submissions;

    @BeforeAll
    static void setUp() {
        support = new DaoTestSupport();
        answers = new AnswerDao(support.dsl());
        submissions = new SubmissionDao(support.dsl());
    }

    @Test
    void twoSubmissionsKeepOneAnswerAndUpdateLatest() {
        ProblemId pid = seedProblem();
        UserId user = UserId.of(IdGenerator.newId());
        AnswerId[] aidHolder = new AnswerId[1];
        support.dsl().transaction(cfg -> {
            aidHolder[0] = new AnswerDao(cfg.dsl()).upsert(pid, user,
                    new GitHubAnswerRepository("https://github.com/me/x"),
                    LocalDateTime.now());
        });
        AnswerId aid = aidHolder[0];

        CommitHash h1 = new CommitHash("0".repeat(40));
        CommitHash h2 = new CommitHash("1".repeat(40));

        support.dsl().transaction(cfg -> {
            SubmissionDao d = new SubmissionDao(cfg.dsl());
            d.insertAndMarkLatest(new Submission(
                    SubmissionId.newId(), aid, h1, LocalDateTime.now()));
        });
        support.dsl().transaction(cfg -> {
            SubmissionDao d = new SubmissionDao(cfg.dsl());
            d.insertAndMarkLatest(new Submission(
                    SubmissionId.newId(), aid, h2, LocalDateTime.now().plusSeconds(1)));
        });

        assertThat(submissions.countByAnswer(aid)).isEqualTo(2L);

        Optional<Submission> latest = submissions.findLatest(aid);
        assertThat(latest).hasValueSatisfying(s -> assertThat(s.commitHash()).isEqualTo(h2));
    }

    private static ProblemId seedProblem() {
        ProblemId pid = ProblemId.newId();
        ProblemLifecycleId lid = ProblemLifecycleId.newId();
        support.dsl().transaction(cfg -> {
            new ProblemDao(cfg.dsl()).insert(
                    new Problem(pid, new ProblemName("seed"),
                            new GenericProblemRepository("https://example.com/p", "main"), lid),
                    new ProblemLifecycle(lid, pid, ProblemStatus.ACTIVE));
        });
        return pid;
    }
}
