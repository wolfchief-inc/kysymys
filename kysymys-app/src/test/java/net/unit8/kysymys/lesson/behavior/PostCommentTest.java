package net.unit8.kysymys.lesson.behavior;

import net.unit8.kysymys.lesson.dao.DaoTestSupport;
import net.unit8.kysymys.lesson.dao.ReviewCommentDao;
import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PostCommentTest {
    private static final DaoTestSupport support = new DaoTestSupport();
    private static final CreateProblem create = new CreateProblem(support.dsl());
    private static final SubmitAnswer submit = new SubmitAnswer(support.dsl());
    private static final PostComment post = new PostComment(support.dsl());

    @Test
    void postsCommentForExistingAnswer() {
        UserId teacher = UserId.of(IdGenerator.newId());
        UserId student = UserId.of(IdGenerator.newId());
        Problem p = create.apply(new CreateProblem.Input(
                new ProblemName("X"),
                new GenericProblemRepository("https://example.com/x", "main"),
                teacher, LocalDateTime.now()));
        SubmitAnswer.Output answer = submit.apply(new SubmitAnswer.Input(
                p.id(), student,
                new GenericAnswerRepository("https://example.com/me"),
                new CommitHash("0".repeat(40)),
                LocalDateTime.now())).orElseThrow();

        Optional<ReviewComment> c = post.apply(new PostComment.Input(
                answer.answer().id(), teacher,
                new Description("looks good"),
                LocalDateTime.now()));

        assertThat(c).isPresent();
        assertThat(new ReviewCommentDao(support.dsl())
                .listByAnswer(answer.answer().id())).hasSize(1);
    }

    @Test
    void postingOnUnknownAnswerReturnsEmpty() {
        Optional<ReviewComment> c = post.apply(new PostComment.Input(
                AnswerId.newId(),
                UserId.of(IdGenerator.newId()),
                new Description("ghost"),
                LocalDateTime.now()));
        assertThat(c).isEmpty();
    }
}
