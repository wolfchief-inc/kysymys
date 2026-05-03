package net.unit8.kysymys.lesson.resource;

import net.unit8.kysymys.lesson.data.*;
import net.unit8.raoh.decode.Decoder;
import tools.jackson.databind.JsonNode;

import java.util.Map;

import static net.unit8.raoh.json.JsonDecoders.*;

public final class AnswerJsonDecoders {
    private AnswerJsonDecoders() {}

    private static final Decoder<JsonNode, AnswerRepository> REPOSITORY = discriminate("type", Map.of(
            "github",    field("url", string().minLength(1).maxLength(255))
                    .map(url -> (AnswerRepository) new GitHubAnswerRepository(url)),
            "bitbucket", field("url", string().minLength(1).maxLength(255))
                    .map(url -> (AnswerRepository) new BitBucketAnswerRepository(url)),
            "generic",   field("url", string().minLength(1).maxLength(255))
                    .map(url -> (AnswerRepository) new GenericAnswerRepository(url))
    ));

    public static final Decoder<JsonNode, SubmitInput> SUBMIT = combine(
            field("repository", REPOSITORY),
            field("commitHash", string().fixedLength(40)).map(CommitHash::new)
    ).map(SubmitInput::new);

    public record SubmitInput(AnswerRepository repository, CommitHash commitHash) {}
}
