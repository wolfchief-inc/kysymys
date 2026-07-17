package net.unit8.kysymys.activity.resource;

import net.unit8.kysymys.activity.data.ActivityKind;
import net.unit8.raoh.decode.Decoder;
import org.jspecify.annotations.Nullable;
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
            nullableField("problemId", string().minLength(1).maxLength(64)),
            nullableField("detail", string().maxLength(2000))
    ).map(RecordActivityInput::new);

    /** Decoder output for POST /activity. */
    public record RecordActivityInput(ActivityKind kind, @Nullable String problemId, @Nullable String detail) {}
}
