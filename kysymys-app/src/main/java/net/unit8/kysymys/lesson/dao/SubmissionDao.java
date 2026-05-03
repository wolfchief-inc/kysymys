package net.unit8.kysymys.lesson.dao;

import net.unit8.kysymys.lesson.data.*;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

/**
 * Manages {@code submissions} (history) and {@code latest_submissions}
 * (a single-row pointer per Answer).
 */
public class SubmissionDao {
    private static final Field<String> ID = field("id", String.class);
    private static final Field<String> ANSWER_ID = field("answer_id", String.class);
    private static final Field<String> COMMIT_HASH = field("commit_hash", String.class);
    private static final Field<LocalDateTime> SUBMITTED_AT = field("submitted_at", LocalDateTime.class);
    private static final Field<String> SUBMISSION_ID = field("submission_id", String.class);

    private final DSLContext dsl;

    public SubmissionDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void insertAndMarkLatest(Submission s) {
        dsl.insertInto(table("submissions"), ID, ANSWER_ID, COMMIT_HASH, SUBMITTED_AT)
                .values(s.id().value(), s.answerId().value(), s.commitHash().value(), s.submittedAt())
                .execute();

        // Upsert into latest_submissions (PK is answer_id alone after V1 fix).
        int updated = dsl.update(table("latest_submissions"))
                .set(SUBMISSION_ID, s.id().value())
                .where(ANSWER_ID.eq(s.answerId().value()))
                .execute();
        if (updated == 0) {
            dsl.insertInto(table("latest_submissions"), ANSWER_ID, SUBMISSION_ID)
                    .values(s.answerId().value(), s.id().value())
                    .execute();
        }
    }

    public long countByAnswer(AnswerId answerId) {
        return dsl.selectCount().from(table("submissions"))
                .where(ANSWER_ID.eq(answerId.value()))
                .fetchOne(0, Long.class);
    }

    public Optional<Submission> findLatest(AnswerId answerId) {
        Record rec = dsl.select(ID, ANSWER_ID, COMMIT_HASH, SUBMITTED_AT)
                .from(table("submissions"))
                .where(ID.eq(
                        dsl.select(SUBMISSION_ID).from(table("latest_submissions"))
                                .where(ANSWER_ID.eq(answerId.value()))
                ))
                .fetchOne();
        return Optional.ofNullable(rec).map(r -> new Submission(
                new SubmissionId(r.get(ID)),
                new AnswerId(r.get(ANSWER_ID)),
                new CommitHash(r.get(COMMIT_HASH)),
                r.get(SUBMITTED_AT)
        ));
    }
}
