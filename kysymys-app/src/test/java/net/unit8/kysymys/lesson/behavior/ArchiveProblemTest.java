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

class ArchiveProblemTest {
    private static DaoTestSupport support;
    private static CreateProblem create;
    private static ArchiveProblem archive;

    @BeforeAll
    static void setUp() {
        support = new DaoTestSupport();
        create = new CreateProblem(support.dsl());
        archive = new ArchiveProblem(support.dsl());
    }

    @Test
    void archiveSwitchesStatusAndAppendsEvent() {
        UserId teacher = UserId.of(IdGenerator.newId());
        Problem original = create.apply(new CreateProblem.Input(
                new ProblemName("X"),
                new GenericProblemRepository("https://example.com/x", "main"),
                teacher,
                LocalDateTime.now()));

        boolean ok = archive.apply(new ArchiveProblem.Input(
                original.id(), teacher, LocalDateTime.now()));

        assertThat(ok).isTrue();
        assertThat(new ProblemDao(support.dsl()).findStatus(original.lifecycleId()))
                .hasValue(ProblemStatus.ARCHIVED);
        assertThat(new ProblemEventDao(support.dsl()).countByLifecycle(original.lifecycleId()))
                .isEqualTo(2L);  // created + archived
    }
}
