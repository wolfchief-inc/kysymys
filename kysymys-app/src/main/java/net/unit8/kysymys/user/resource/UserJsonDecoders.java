package net.unit8.kysymys.user.resource;

import net.unit8.kysymys.user.data.EmailAddress;
import net.unit8.kysymys.user.data.UserId;
import net.unit8.kysymys.user.data.UserName;
import net.unit8.raoh.decode.Decoder;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import static net.unit8.raoh.json.JsonDecoders.*;

/**
 * Raoh decoders for every User context request body.
 */
public final class UserJsonDecoders {
    private UserJsonDecoders() {}

    public static final Decoder<JsonNode, UpdateProfileInput> UPDATE_PROFILE = combine(
            nullableField("email", string().minLength(1).maxLength(100).map(EmailAddress::of)),
            nullableField("name", string().minLength(1).maxLength(100).map(UserName::of))
    ).map(UpdateProfileInput::new);

    /** Decoder output for POST /offers — body carries the target user. */
    public static final Decoder<JsonNode, UserId> OFFER =
            field("targetUserId", string().fixedLength(21)).map(UserId::of);

    /** Decoder output for POST /grant-teacher-role — body carries the target user. */
    public static final Decoder<JsonNode, UserId> GRANT_TEACHER_ROLE =
            field("targetUserId", string().fixedLength(21)).map(UserId::of);

    public record UpdateProfileInput(@Nullable EmailAddress email, @Nullable UserName name) {}
}
