package net.unit8.kysymys.lesson.dao;

import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ProblemDaoTest {
    private static DaoTestSupport support;
    private static ProblemDao dao;
    private static ProblemEventDao events;

    @BeforeAll
    static void setUp() {
        support = new DaoTestSupport();
        dao = new ProblemDao(support.dsl());
        events = new ProblemEventDao(support.dsl());
    }

    @Test
    void saveAndFindById() {
        ProblemId pid = ProblemId.newId();
        ProblemLifecycleId lid = ProblemLifecycleId.newId();
        Problem p = new Problem(
                pid,
                new ProblemName("FizzBuzz"),
                new GitHubProblemRepository(
                        "https://github.com/kysymys/fizzbuzz.git", "main", "/README.md"),
                lid);
        ProblemLifecycle lc = new ProblemLifecycle(lid, pid, ProblemStatus.ACTIVE);

        support.dsl().transaction(cfg -> {
            new ProblemDao(cfg.dsl()).insert(p, lc);
        });

        Optional<Problem> loaded = dao.findById(pid);
        assertThat(loaded).hasValueSatisfying(found -> {
            assertThat(found.id()).isEqualTo(pid);
            assertThat(found.name().value()).isEqualTo("FizzBuzz");
            assertThat(found.repository()).isInstanceOf(GitHubProblemRepository.class);
        });
    }

    @Test
    void updateLifecycleStatus() {
        ProblemId pid = ProblemId.newId();
        ProblemLifecycleId lid = ProblemLifecycleId.newId();
        Problem p = new Problem(pid, new ProblemName("Echo"),
                new GenericProblemRepository("https://example.com/echo", "main"), lid);

        support.dsl().transaction(cfg -> {
            new ProblemDao(cfg.dsl()).insert(p,
                    new ProblemLifecycle(lid, pid, ProblemStatus.ACTIVE));
            new ProblemDao(cfg.dsl()).updateStatus(lid, ProblemStatus.ARCHIVED);
        });

        ProblemStatus status = dao.findStatus(lid).orElseThrow();
        assertThat(status).isEqualTo(ProblemStatus.ARCHIVED);
    }

    @Test
    void persistCreatedEvent() {
        ProblemId pid = ProblemId.newId();
        ProblemLifecycleId lid = ProblemLifecycleId.newId();
        Problem p = new Problem(pid, new ProblemName("Echo"),
                new GenericProblemRepository("https://example.com/echo", "main"), lid);
        UserId teacher = UserId.of(IdGenerator.newId());

        support.dsl().transaction(cfg -> {
            new ProblemDao(cfg.dsl()).insert(p,
                    new ProblemLifecycle(lid, pid, ProblemStatus.ACTIVE));
            new ProblemEventDao(cfg.dsl()).insert(new ProblemCreatedEvent(
                    ProblemEventId.newId(),
                    lid,
                    LocalDateTime.now(),
                    teacher));
        });

        long count = events.countByLifecycle(lid);
        assertThat(count).isEqualTo(1L);
    }

    @Test
    void listAllReturnsInsertedProblems() {
        ProblemId pid = ProblemId.newId();
        ProblemLifecycleId lid = ProblemLifecycleId.newId();
        support.dsl().transaction(cfg -> {
            new ProblemDao(cfg.dsl()).insert(
                    new Problem(pid, new ProblemName("ListTest"),
                            new GenericProblemRepository("https://example.com/x", "main"), lid),
                    new ProblemLifecycle(lid, pid, ProblemStatus.ACTIVE));
        });

        assertThat(dao.listActive())
                .extracting(Problem::id)
                .contains(pid);
    }
}
