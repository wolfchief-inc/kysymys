package net.unit8.kysymys.lesson.behavior;

import net.unit8.kysymys.lesson.dao.DaoTestSupport;
import net.unit8.kysymys.lesson.dao.ProblemEventDao;
import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateProblemTest {
    private static DaoTestSupport support;
    private static CreateProblem create;
    private static UpdateProblem update;

    @BeforeAll
    static void setUp() {
        support = new DaoTestSupport();
        create = new CreateProblem(support.dsl());
        update = new UpdateProblem(support.dsl());
    }

    @Test
    void updateRenamesAndAppendsEvent() {
        UserId teacher = UserId.of(IdGenerator.newId());
        Problem original = create.apply(new CreateProblem.Input(
                new ProblemName("Old name"),
                new GenericProblemRepository("https://example.com/x", "main"),
                teacher,
                LocalDateTime.now()));

        Optional<Problem> updated = update.apply(new UpdateProblem.Input(
                original.id(),
                new ProblemName("New name"),
                new GenericProblemRepository("https://example.com/x", "main"),
                teacher,
                LocalDateTime.now()));

        assertThat(updated).hasValueSatisfying(p ->
                assertThat(p.name().value()).isEqualTo("New name"));
        assertThat(new ProblemEventDao(support.dsl()).countByLifecycle(original.lifecycleId()))
                .isEqualTo(2L);  // created + updated
    }

    @Test
    void updateOnMissingReturnsEmpty() {
        Optional<Problem> result = update.apply(new UpdateProblem.Input(
                ProblemId.newId(),
                new ProblemName("ghost"),
                new GenericProblemRepository("https://example.com/x", "main"),
                UserId.of(IdGenerator.newId()),
                LocalDateTime.now()));
        assertThat(result).isEmpty();
    }
}
