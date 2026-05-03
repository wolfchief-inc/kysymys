package net.unit8.kysymys.lesson.dao;

import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;
import org.jooq.Field;

import java.time.LocalDateTime;
import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class ReviewCommentDao {
    private static final Field<String> ID = field("id", String.class);
    private static final Field<String> ANSWER_ID = field("answer_id", String.class);
    private static final Field<String> COMMENTER_ID = field("commenter_id", String.class);
    private static final Field<String> DESCRIPTION = field("description", String.class);
    private static final Field<LocalDateTime> POSTED_AT = field("posted_at", LocalDateTime.class);

    private final DSLContext dsl;

    public ReviewCommentDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void insert(ReviewComment c) {
        dsl.insertInto(table("review_comments"),
                        ID, ANSWER_ID, COMMENTER_ID, DESCRIPTION, POSTED_AT)
                .values(c.id().value(), c.answerId().value(), c.commenterId().value(),
                        c.description().value(), c.postedAt())
                .execute();
    }

    public List<ReviewComment> listByAnswer(AnswerId answerId) {
        return dsl.select(ID, ANSWER_ID, COMMENTER_ID, DESCRIPTION, POSTED_AT)
                .from(table("review_comments"))
                .where(ANSWER_ID.eq(answerId.value()))
                .orderBy(POSTED_AT.asc())
                .fetch(r -> new ReviewComment(
                        new CommentId(r.get(ID)),
                        new AnswerId(r.get(ANSWER_ID)),
                        UserId.of(r.get(COMMENTER_ID)),
                        new Description(r.get(DESCRIPTION)),
                        r.get(POSTED_AT)
                ));
    }
}
