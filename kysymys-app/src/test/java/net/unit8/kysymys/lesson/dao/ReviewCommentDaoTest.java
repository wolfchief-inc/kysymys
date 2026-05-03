package net.unit8.kysymys.lesson.dao;

import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewCommentDaoTest {
    private static DaoTestSupport support;
    private static ReviewCommentDao comments;

    @BeforeAll
    static void setUp() {
        support = new DaoTestSupport();
        comments = new ReviewCommentDao(support.dsl());
    }

    @Test
    void insertAndListByAnswerInPostedOrder() {
        AnswerId aid = seedAnswer();
        UserId user = UserId.of(IdGenerator.newId());

        support.dsl().transaction(cfg -> {
            ReviewCommentDao d = new ReviewCommentDao(cfg.dsl());
            d.insert(new ReviewComment(
                    CommentId.newId(), aid, user,
                    new Description("first"), LocalDateTime.now()));
            d.insert(new ReviewComment(
                    CommentId.newId(), aid, user,
                    new Description("second"), LocalDateTime.now().plusSeconds(1)));
        });

        List<ReviewComment> list = comments.listByAnswer(aid);
        assertThat(list).hasSize(2);
        assertThat(list.get(0).description().value()).isEqualTo("first");
        assertThat(list.get(1).description().value()).isEqualTo("second");
    }

    private static AnswerId seedAnswer() {
        ProblemId pid = ProblemId.newId();
        ProblemLifecycleId lid = ProblemLifecycleId.newId();
        UserId user = UserId.of(IdGenerator.newId());
        AnswerId[] holder = new AnswerId[1];
        support.dsl().transaction(cfg -> {
            new ProblemDao(cfg.dsl()).insert(
                    new Problem(pid, new ProblemName("seed"),
                            new GenericProblemRepository("https://example.com/p", "main"), lid),
                    new ProblemLifecycle(lid, pid, ProblemStatus.ACTIVE));
            holder[0] = new AnswerDao(cfg.dsl()).upsert(pid, user,
                    new GenericAnswerRepository("https://example.com/me"), LocalDateTime.now());
        });
        return holder[0];
    }
}
