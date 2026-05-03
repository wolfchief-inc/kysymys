package net.unit8.kysymys.lesson.behavior;

import net.unit8.kysymys.lesson.dao.DaoTestSupport;
import net.unit8.kysymys.lesson.dao.ProblemDao;
import net.unit8.kysymys.lesson.dao.ProblemEventDao;
import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CreateProblemTest {
    private static DaoTestSupport support;
    private static CreateProblem behavior;

    @BeforeAll
    static void setUp() {
        support = new DaoTestSupport();
        behavior = new CreateProblem(support.dsl());
    }

    @Test
    void insertsProblemLifecycleAndCreatedEvent() {
        UserId teacher = UserId.of(IdGenerator.newId());
        Problem p = behavior.apply(
                new CreateProblem.Input(
                        new ProblemName("Loop unrolling"),
                        new GitHubProblemRepository(
                                "https://github.com/k/loop", "main", "/README.md"),
                        teacher,
                        LocalDateTime.now()));

        ProblemDao problems = new ProblemDao(support.dsl());
        ProblemEventDao events = new ProblemEventDao(support.dsl());

        assertThat(problems.findById(p.id())).isPresent();
        assertThat(problems.findStatus(p.lifecycleId())).hasValue(ProblemStatus.ACTIVE);
        assertThat(events.countByLifecycle(p.lifecycleId())).isEqualTo(1L);
    }
}
