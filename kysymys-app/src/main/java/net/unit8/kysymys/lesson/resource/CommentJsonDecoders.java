package net.unit8.kysymys.lesson.resource;

import net.unit8.kysymys.lesson.data.Description;
import net.unit8.raoh.decode.Decoder;
import tools.jackson.databind.JsonNode;

import static net.unit8.raoh.json.JsonDecoders.field;
import static net.unit8.raoh.json.JsonDecoders.string;

public final class CommentJsonDecoders {
    private CommentJsonDecoders() {}

    public static final Decoder<JsonNode, PostInput> POST =
            field("description", string().minLength(1).maxLength(4000))
                    .map(d -> new PostInput(new Description(d)));

    public record PostInput(Description description) {}
}
