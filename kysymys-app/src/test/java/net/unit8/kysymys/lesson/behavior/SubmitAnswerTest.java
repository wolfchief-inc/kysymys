package net.unit8.kysymys.lesson.behavior;

import net.unit8.kysymys.lesson.dao.DaoTestSupport;
import net.unit8.kysymys.lesson.dao.SubmissionDao;
import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SubmitAnswerTest {
    private static DaoTestSupport support;
    private static CreateProblem create;
    private static SubmitAnswer submit;

    @BeforeAll
    static void setUp() {
        support = new DaoTestSupport();
        create = new CreateProblem(support.dsl());
        submit = new SubmitAnswer(support.dsl());
    }

    @Test
    void twoSubmitsKeepOneAnswerAndAdvanceLatest() {
        UserId teacher = UserId.of(IdGenerator.newId());
        UserId student = UserId.of(IdGenerator.newId());
        Problem p = create.apply(new CreateProblem.Input(
                new ProblemName("X"),
                new GenericProblemRepository("https://example.com/x", "main"),
                teacher, LocalDateTime.now()));

        AnswerRepository repo = new GitHubAnswerRepository("https://github.com/me/x");
        CommitHash h1 = new CommitHash("0".repeat(40));
        CommitHash h2 = new CommitHash("1".repeat(40));

        Optional<SubmitAnswer.Output> first = submit.apply(new SubmitAnswer.Input(
                p.id(), student, repo, h1, LocalDateTime.now()));
        Optional<SubmitAnswer.Output> second = submit.apply(new SubmitAnswer.Input(
                p.id(), student, repo, h2, LocalDateTime.now().plusMinutes(1)));

        assertThat(first).isPresent();
        assertThat(second).isPresent();
        assertThat(first.get().answer().id()).isEqualTo(second.get().answer().id());

        SubmissionDao subDao = new SubmissionDao(support.dsl());
        assertThat(subDao.countByAnswer(first.get().answer().id())).isEqualTo(2L);
        assertThat(subDao.findLatest(first.get().answer().id()))
                .hasValueSatisfying(s -> assertThat(s.commitHash()).isEqualTo(h2));
    }

    @Test
    void submitOnUnknownProblemReturnsEmpty() {
        Optional<SubmitAnswer.Output> result = submit.apply(new SubmitAnswer.Input(
                ProblemId.newId(),
                UserId.of(IdGenerator.newId()),
                new GenericAnswerRepository("https://example.com/me"),
                new CommitHash("a".repeat(40)),
                LocalDateTime.now()));
        assertThat(result).isEmpty();
    }
}
