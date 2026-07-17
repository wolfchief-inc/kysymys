package net.unit8.kysymys.lesson.dao;

import net.unit8.kysymys.lesson.data.Answer;
import net.unit8.kysymys.lesson.data.AnswerId;
import net.unit8.kysymys.lesson.data.AnswerRepository;
import net.unit8.kysymys.lesson.data.BitBucketAnswerRepository;
import net.unit8.kysymys.lesson.data.BitBucketProblemRepository;
import net.unit8.kysymys.lesson.data.CommentId;
import net.unit8.kysymys.lesson.data.CommitHash;
import net.unit8.kysymys.lesson.data.Description;
import net.unit8.kysymys.lesson.data.GenericAnswerRepository;
import net.unit8.kysymys.lesson.data.GenericProblemRepository;
import net.unit8.kysymys.lesson.data.GitHubAnswerRepository;
import net.unit8.kysymys.lesson.data.GitHubProblemRepository;
import net.unit8.kysymys.lesson.data.Problem;
import net.unit8.kysymys.lesson.data.ProblemId;
import net.unit8.kysymys.lesson.data.ProblemLifecycleId;
import net.unit8.kysymys.lesson.data.ProblemName;
import net.unit8.kysymys.lesson.data.ProblemRepository;
import net.unit8.kysymys.lesson.data.ReviewComment;
import net.unit8.kysymys.lesson.data.Submission;
import net.unit8.kysymys.lesson.data.SubmissionId;
import net.unit8.kysymys.user.data.UserId;
import net.unit8.raoh.decode.Decoder;
import org.jooq.Record;

import java.time.LocalDateTime;
import java.util.Map;

import static net.unit8.raoh.decode.ObjectDecoders.dateTime;
import static net.unit8.raoh.decode.ObjectDecoders.string;
import static net.unit8.raoh.jooq.JooqRecordDecoders.combine;
import static net.unit8.raoh.jooq.JooqRecordDecoders.discriminate;
import static net.unit8.raoh.jooq.JooqRecordDecoders.field;

/**
 * raoh-jooq decoders mapping Lesson-context rows into aggregates — the read-side
 * mirror of {@code LessonJsonDecoders}. Kept per bounded context (one class for the
 * whole context) to match the request-side convention.
 *
 * <p>The {@code runner} column is the discriminator for {@link ProblemRepository},
 * exactly as {@code "type"} is on the JSON side.
 */
public final class LessonRecordDecoders {
    private LessonRecordDecoders() {}

    private static final Decoder<Record, ProblemRepository> PROBLEM_REPOSITORY = discriminate("runner", Map.of(
            "github", combine(
                    field("repository_url", string()), field("branch", string()), field("readme_path", string()))
                    .map((url, branch, readme) -> (ProblemRepository) new GitHubProblemRepository(url, branch, readme)),
            "bitbucket", combine(
                    field("repository_url", string()), field("branch", string()), field("readme_path", string()))
                    .map((url, branch, readme) -> (ProblemRepository) new BitBucketProblemRepository(url, branch, readme)),
            "generic", combine(
                    field("repository_url", string()), field("branch", string()))
                    .map((url, branch) -> (ProblemRepository) new GenericProblemRepository(url, branch))
    ));

    public static final Decoder<Record, Problem> PROBLEM = combine(
            field("id", string()).map(ProblemId::new),
            field("name", string()).map(ProblemName::new),
            PROBLEM_REPOSITORY,
            field("problem_lifecycle_id", string()).map(ProblemLifecycleId::new)
    ).map(Problem::new);

    public static final Decoder<Record, Answer> ANSWER = combine(
            field("id", string()).map(AnswerId::new),
            field("problem_id", string()).map(ProblemId::new),
            field("answerer_id", string()).map(UserId::of),
            field("repository_url", string()).map(LessonRecordDecoders::answerRepositoryFromUrl)
    ).map((id, problemId, answererId, repo) ->
            new Answer(id, problemId, answererId, repo, LocalDateTime.MIN));

    public static final Decoder<Record, Submission> SUBMISSION = combine(
            field("id", string()).map(SubmissionId::new),
            field("answer_id", string()).map(AnswerId::new),
            field("commit_hash", string()).map(CommitHash::new),
            field("submitted_at", dateTime())
    ).map(Submission::new);

    public static final Decoder<Record, ReviewComment> REVIEW_COMMENT = combine(
            field("id", string()).map(CommentId::new),
            field("answer_id", string()).map(AnswerId::new),
            field("commenter_id", string()).map(UserId::of),
            field("description", string()).map(Description::new),
            field("posted_at", dateTime())
    ).map(ReviewComment::new);

    /** The {@code answers} table carries no discriminator column; recover the subtype from the URL prefix. */
    private static AnswerRepository answerRepositoryFromUrl(String url) {
        if (url.startsWith("https://github.com/")) return new GitHubAnswerRepository(url);
        if (url.startsWith("https://bitbucket.org/")) return new BitBucketAnswerRepository(url);
        return new GenericAnswerRepository(url);
    }
}
