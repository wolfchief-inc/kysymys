package net.unit8.kysymys.activity.resource;

import net.unit8.kysymys.activity.data.ActivityKind;
import net.unit8.raoh.decode.Decoder;
import tools.jackson.databind.JsonNode;

import static net.unit8.raoh.json.JsonDecoders.*;

/**
 * Raoh decoder for the activity-telemetry request body
 * ({@code POST /activity}).
 *
 * <p>Shape: {@code {"kind": "BUILD_FAILURE", "problemId": "...", "detail": "..."}}.
 * {@code kind} is required and constrained to the {@link ActivityKind} names;
 * {@code problemId} and {@code detail} are optional.
 */
public final class ActivityJsonDecoders {
    private ActivityJsonDecoders() {}

    public static final Decoder<JsonNode, RecordActivityInput> RECORD_ACTIVITY = combine(
            field("kind", enumOf(ActivityKind.class)),
            optionalField("problemId", string().minLength(1).maxLength(64)),
            optionalField("detail", string().maxLength(2000))
    ).map((kind, problemId, detail) ->
            new RecordActivityInput(kind, problemId.orElse(null), detail.orElse(null)));

    /** Decoder output for POST /activity. */
    public record RecordActivityInput(ActivityKind kind, String problemId, String detail) {}
}
