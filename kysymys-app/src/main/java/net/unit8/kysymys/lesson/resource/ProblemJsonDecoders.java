package net.unit8.kysymys.lesson.resource;

import net.unit8.kysymys.lesson.data.*;
import net.unit8.raoh.decode.Decoder;
import tools.jackson.databind.JsonNode;

import java.util.Map;

import static net.unit8.raoh.json.JsonDecoders.*;

/**
 * Raoh decoders for Problem-related request bodies.
 *
 * <p>Repository discriminator: {@code "type"} chooses between
 * {@code github}, {@code bitbucket}, {@code generic}.
 */
public final class ProblemJsonDecoders {
    private ProblemJsonDecoders() {}

    private static final Decoder<JsonNode, ProblemRepository> REPOSITORY_DECODER =
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

    public static final Decoder<JsonNode, CreateInput> CREATE = combine(
            field("name", string().minLength(1).maxLength(100)).map(ProblemName::new),
            field("repository", REPOSITORY_DECODER)
    ).map(CreateInput::new);

    public static final Decoder<JsonNode, UpdateInput> UPDATE = combine(
            field("name", string().minLength(1).maxLength(100)).map(ProblemName::new),
            field("repository", REPOSITORY_DECODER)
    ).map(UpdateInput::new);

    /** Decoder output for POST /problems. */
    public record CreateInput(ProblemName name, ProblemRepository repository) {}

    /** Decoder output for PUT /problems/:id. */
    public record UpdateInput(ProblemName name, ProblemRepository repository) {}
}
