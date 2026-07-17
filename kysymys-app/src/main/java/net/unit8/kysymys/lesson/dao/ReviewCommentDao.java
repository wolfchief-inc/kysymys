package net.unit8.kysymys.lesson.dao;

import net.unit8.kysymys.lesson.data.*;
import net.unit8.raoh.Result;
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
        return Result.traverse(
                dsl.select(ID, ANSWER_ID, COMMENTER_ID, DESCRIPTION, POSTED_AT)
                        .from(table("review_comments"))
                        .where(ANSWER_ID.eq(answerId.value()))
                        .orderBy(POSTED_AT.asc())
                        .fetch(),
                LessonRecordDecoders.REVIEW_COMMENT::decode).getOrThrow();
    }
}
