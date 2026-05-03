package net.unit8.kysymys.lesson.dao;

import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class AnswerDao {
    private static final Field<String> ID = field("id", String.class);
    private static final Field<String> PROBLEM_ID = field("problem_id", String.class);
    private static final Field<String> ANSWERER_ID = field("answerer_id", String.class);
    private static final Field<String> REPOSITORY_URL = field("repository_url", String.class);

    private final DSLContext dsl;

    public AnswerDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * Returns the AnswerId for {@code (problemId, answererId)}, inserting a row
     * if none exists. Mirrors the legacy invariant of "one Answer per
     * (problem, answerer)".
     */
    public AnswerId upsert(ProblemId problemId, UserId answererId,
                           AnswerRepository repository, LocalDateTime when) {
        Optional<String> existing = Optional.ofNullable(
                dsl.select(ID).from(table("answers"))
                        .where(PROBLEM_ID.eq(problemId.value()))
                        .and(ANSWERER_ID.eq(answererId.value()))
                        .fetchOne(ID));
        if (existing.isPresent()) {
            return new AnswerId(existing.get());
        }
        AnswerId fresh = AnswerId.newId();
        dsl.insertInto(table("answers"), ID, PROBLEM_ID, ANSWERER_ID, REPOSITORY_URL)
                .values(fresh.value(), problemId.value(), answererId.value(), repository.url())
                .execute();
        return fresh;
    }

    public Optional<Answer> findById(AnswerId id) {
        Record rec = dsl.select(ID, PROBLEM_ID, ANSWERER_ID, REPOSITORY_URL)
                .from(table("answers"))
                .where(ID.eq(id.value()))
                .fetchOne();
        return Optional.ofNullable(rec).map(AnswerDao::mapAnswer);
    }

    public List<Answer> listByAnswerer(UserId answererId) {
        return dsl.select(ID, PROBLEM_ID, ANSWERER_ID, REPOSITORY_URL)
                .from(table("answers"))
                .where(ANSWERER_ID.eq(answererId.value()))
                .fetch(AnswerDao::mapAnswer);
    }

    /**
     * Lists answers whose {@code answerer_id} is in the provided list.
     * Used by Lesson's ListFollowerAnswers (callers pass in the followee
     * user ids resolved from the User context's ConnectionDao).
     */
    public List<Answer> listByAnswerers(List<UserId> answererIds) {
        if (answererIds == null || answererIds.isEmpty()) {
            return List.of();
        }
        List<String> values = answererIds.stream().map(UserId::value).toList();
        return dsl.select(ID, PROBLEM_ID, ANSWERER_ID, REPOSITORY_URL)
                .from(table("answers"))
                .where(ANSWERER_ID.in(values))
                .fetch(AnswerDao::mapAnswer);
    }

    private static Answer mapAnswer(Record r) {
        // Recover the sealed AnswerRepository subtype from the URL prefix.
        // (The "answers" table doesn't carry a discriminator column today.)
        String url = r.get(REPOSITORY_URL);
        AnswerRepository repo;
        if (url.startsWith("https://github.com/")) repo = new GitHubAnswerRepository(url);
        else if (url.startsWith("https://bitbucket.org/")) repo = new BitBucketAnswerRepository(url);
        else repo = new GenericAnswerRepository(url);

        // lastAnsweredAt is not persisted on the answers table in V1; the
        // resource layer overlays the latest submission's submitted_at when
        // building the response body.
        return new Answer(
                new AnswerId(r.get(ID)),
                new ProblemId(r.get(PROBLEM_ID)),
                UserId.of(r.get(ANSWERER_ID)),
                repo,
                LocalDateTime.MIN
        );
    }
}
