package net.unit8.kysymys.lesson.dao;

import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AnswerDaoTest {
    private static DaoTestSupport support;
    private static AnswerDao dao;

    @BeforeAll
    static void setUp() {
        support = new DaoTestSupport();
        dao = new AnswerDao(support.dsl());
    }

    @Test
    void upsertReturnsExistingIdForSameAnswerer() {
        ProblemId pid = seedProblem();
        UserId answerer = UserId.of(IdGenerator.newId());
        AnswerRepository repo = new GitHubAnswerRepository("https://github.com/me/x");

        AnswerId[] ids = new AnswerId[2];
        support.dsl().transaction(cfg -> {
            ids[0] = new AnswerDao(cfg.dsl()).upsert(pid, answerer, repo, LocalDateTime.now());
        });
        support.dsl().transaction(cfg -> {
            ids[1] = new AnswerDao(cfg.dsl()).upsert(pid, answerer, repo, LocalDateTime.now().plusMinutes(1));
        });

        assertThat(ids[0]).isEqualTo(ids[1]);
        assertThat(dao.listByAnswerer(answerer))
                .extracting(Answer::id)
                .containsExactly(ids[0]);
    }

    private static ProblemId seedProblem() {
        ProblemId pid = ProblemId.newId();
        ProblemLifecycleId lid = ProblemLifecycleId.newId();
        Problem p = new Problem(pid, new ProblemName("seed"),
                new GenericProblemRepository("https://example.com/p", "main"), lid);
        support.dsl().transaction(cfg -> {
            new ProblemDao(cfg.dsl()).insert(p,
                    new ProblemLifecycle(lid, pid, ProblemStatus.ACTIVE));
        });
        return pid;
    }
}
