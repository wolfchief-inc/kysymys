package net.unit8.kysymys.lesson.resource;

import net.unit8.kysymys.lesson.data.*;
import net.unit8.raoh.encode.Encoder;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.unit8.raoh.encode.MapEncoders.discriminate;
import static net.unit8.raoh.encode.MapEncoders.nested;
import static net.unit8.raoh.encode.MapEncoders.object;
import static net.unit8.raoh.encode.MapEncoders.property;
import static net.unit8.raoh.encode.MapEncoders.variant;
import static net.unit8.raoh.encode.ObjectEncoders.string;

/**
 * JSON response shapes for the Lesson context, built with raoh-encode — the mirror
 * of {@code LessonJsonDecoders} on the request side.
 *
 * <p>Each {@code repository} subtype gets its own {@code object(...)} encoder, and
 * {@code discriminate("type", variant(...))} dispatches on the runtime type and writes
 * the {@code type} tag — the encode-side mirror of the decoder's
 * {@code discriminate("type", ...)}. Fields that aren't part of a single domain object
 * (lifecycle status, the optional latest submission, the comment list) are appended to
 * the encoded base map.
 */
public final class LessonJsonEncoders {
    private LessonJsonEncoders() {}

    // --- repository encoders (one per variant, dispatched by discriminate) ---

    private static final Encoder<GitHubProblemRepository, Map<String, @Nullable Object>> GITHUB_PROBLEM_REPO = object(
            property("url", GitHubProblemRepository::url, string()),
            property("branch", GitHubProblemRepository::branch, string()),
            property("readmePath", GitHubProblemRepository::readmePath, string()));

    private static final Encoder<BitBucketProblemRepository, Map<String, @Nullable Object>> BITBUCKET_PROBLEM_REPO = object(
            property("url", BitBucketProblemRepository::url, string()),
            property("branch", BitBucketProblemRepository::branch, string()),
            property("readmePath", BitBucketProblemRepository::readmePath, string()));

    private static final Encoder<GenericProblemRepository, Map<String, @Nullable Object>> GENERIC_PROBLEM_REPO = object(
            property("url", GenericProblemRepository::url, string()),
            property("branch", GenericProblemRepository::branch, string()));

    private static final Encoder<ProblemRepository, Map<String, @Nullable Object>> PROBLEM_REPOSITORY = discriminate("type",
            variant(GitHubProblemRepository.class, "github", GITHUB_PROBLEM_REPO),
            variant(BitBucketProblemRepository.class, "bitbucket", BITBUCKET_PROBLEM_REPO),
            variant(GenericProblemRepository.class, "generic", GENERIC_PROBLEM_REPO));

    private static final Encoder<GitHubAnswerRepository, Map<String, @Nullable Object>> GITHUB_ANSWER_REPO = object(
            property("url", GitHubAnswerRepository::url, string()));

    private static final Encoder<BitBucketAnswerRepository, Map<String, @Nullable Object>> BITBUCKET_ANSWER_REPO = object(
            property("url", BitBucketAnswerRepository::url, string()));

    private static final Encoder<GenericAnswerRepository, Map<String, @Nullable Object>> GENERIC_ANSWER_REPO = object(
            property("url", GenericAnswerRepository::url, string()));

    private static final Encoder<AnswerRepository, Map<String, @Nullable Object>> ANSWER_REPOSITORY = discriminate("type",
            variant(GitHubAnswerRepository.class, "github", GITHUB_ANSWER_REPO),
            variant(BitBucketAnswerRepository.class, "bitbucket", BITBUCKET_ANSWER_REPO),
            variant(GenericAnswerRepository.class, "generic", GENERIC_ANSWER_REPO));

    // --- aggregate encoders ---

    private static final Encoder<Problem, Map<String, @Nullable Object>> PROBLEM = object(
            property("id", p -> p.id().value(), string()),
            property("name", p -> p.name().value(), string()),
            property("repository", p -> p.repository(), nested(PROBLEM_REPOSITORY)),
            property("problemUrl", p -> p.repository().problemUrl(), string()));

    private static final Encoder<Answer, Map<String, @Nullable Object>> ANSWER = object(
            property("id", a -> a.id().value(), string()),
            property("problemId", a -> a.problemId().value(), string()),
            property("answererId", a -> a.answererId().value(), string()),
            property("repository", a -> a.repository(), nested(ANSWER_REPOSITORY)));

    private static final Encoder<ReviewComment, Map<String, @Nullable Object>> COMMENT = object(
            property("id", c -> c.id().value(), string()),
            property("answerId", c -> c.answerId().value(), string()),
            property("commenterId", c -> c.commenterId().value(), string()),
            property("description", c -> c.description().value(), string()),
            property("postedAt", c -> c.postedAt().toString(), string()));

    public static Map<String, Object> encodeProblem(Problem p, ProblemStatus status) {
        Map<String, Object> body = PROBLEM.encode(p);
        body.put("status", status.name());
        return body;
    }

    public static Map<String, Object> encodeAnswer(Answer a, Optional<Submission> latest) {
        return encodeAnswer(a, latest, List.of());
    }

    public static Map<String, Object> encodeAnswer(Answer a, Optional<Submission> latest,
                                                   List<ReviewComment> comments) {
        Map<String, Object> body = ANSWER.encode(a);
        latest.ifPresent(s -> {
            body.put("latestCommitHash", s.commitHash().value());
            body.put("latestSubmittedAt", s.submittedAt().toString());
            body.put("answerUrl", a.repository().commitUrl(s.commitHash()));
        });
        body.put("comments", comments.stream().map(LessonJsonEncoders::encodeComment).toList());
        return body;
    }

    public static Map<String, Object> encodeComment(ReviewComment c) {
        return COMMENT.encode(c);
    }
}
