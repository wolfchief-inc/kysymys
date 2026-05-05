package net.unit8.kysymys.lesson.resource;

import net.unit8.kysymys.lesson.data.*;
import net.unit8.raoh.decode.Decoder;
import tools.jackson.databind.JsonNode;

import java.util.Map;

import static net.unit8.raoh.json.JsonDecoders.*;

/**
 * Raoh decoders for every Lesson context request body.
 *
 * <p>Repository discriminator: {@code "type"} chooses between
 * {@code github}, {@code bitbucket}, {@code generic}.
 */
public final class LessonJsonDecoders {
    private LessonJsonDecoders() {}

    private static final Decoder<JsonNode, ProblemRepository> PROBLEM_REPOSITORY =
            discriminate("type", Map.of(
                    "github", combine(
                            field("url", string().minLength(1).maxLength(255)),
                            field("branch", string().minLength(1).maxLength(100)),
                            field("readmePath", string().minLength(1).maxLength(100))
                    ).map((url, branch, readmePath) ->
                            (ProblemRepository) new GitHubProblemRepository(url, branch, readmePath)),
                    "bitbucket", combine(
                            field("url", string().minLength(1).maxLength(255)),
                            field("branch", string().minLength(1).maxLength(100)),
                            field("readmePath", string().minLength(1).maxLength(100))
                    ).map((url, branch, readmePath) ->
                            (ProblemRepository) new BitBucketProblemRepository(url, branch, readmePath)),
                    "generic", combine(
                            field("url", string().minLength(1).maxLength(255)),
                            field("branch", string().minLength(1).maxLength(100))
                    ).map((url, branch) ->
                            (ProblemRepository) new GenericProblemRepository(url, branch))
            ));

    private static final Decoder<JsonNode, AnswerRepository> ANSWER_REPOSITORY =
            discriminate("type", Map.of(
                    "github",    field("url", string().minLength(1).maxLength(255))
                            .map(url -> (AnswerRepository) new GitHubAnswerRepository(url)),
                    "bitbucket", field("url", string().minLength(1).maxLength(255))
                            .map(url -> (AnswerRepository) new BitBucketAnswerRepository(url)),
                    "generic",   field("url", string().minLength(1).maxLength(255))
                            .map(url -> (AnswerRepository) new GenericAnswerRepository(url))
            ));

    public static final Decoder<JsonNode, CreateProblemInput> CREATE_PROBLEM = combine(
            field("name", string().minLength(1).maxLength(100)).map(ProblemName::new),
            field("repository", PROBLEM_REPOSITORY)
    ).map(CreateProblemInput::new);

    public static final Decoder<JsonNode, UpdateProblemInput> UPDATE_PROBLEM = combine(
            field("name", string().minLength(1).maxLength(100)).map(ProblemName::new),
            field("repository", PROBLEM_REPOSITORY)
    ).map(UpdateProblemInput::new);

    public static final Decoder<JsonNode, SubmitAnswerInput> SUBMIT_ANSWER = combine(
            field("repository", ANSWER_REPOSITORY),
            field("commitHash", string().fixedLength(40)).map(CommitHash::new)
    ).map(SubmitAnswerInput::new);

    public static final Decoder<JsonNode, PostCommentInput> POST_COMMENT =
            field("description", string().minLength(1).maxLength(4000))
                    .map(d -> new PostCommentInput(new Description(d)));

    /** Decoder output for POST /problems. */
    public record CreateProblemInput(ProblemName name, ProblemRepository repository) {}

    /** Decoder output for PUT /problems/:id. */
    public record UpdateProblemInput(ProblemName name, ProblemRepository repository) {}

    /** Decoder output for POST /problems/:id/answers. */
    public record SubmitAnswerInput(AnswerRepository repository, CommitHash commitHash) {}

    /** Decoder output for POST /answers/:id/comments. */
    public record PostCommentInput(Description description) {}
}
