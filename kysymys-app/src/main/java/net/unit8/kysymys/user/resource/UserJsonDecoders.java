package net.unit8.kysymys.user.resource;

import net.unit8.kysymys.user.data.EmailAddress;
import net.unit8.kysymys.user.data.UserName;
import net.unit8.raoh.decode.Decoder;
import tools.jackson.databind.JsonNode;

import static net.unit8.raoh.json.JsonDecoders.*;

public final class UserJsonDecoders {
    private UserJsonDecoders() {}

    public static final Decoder<JsonNode, UpdateProfileInput> UPDATE_PROFILE = combine(
            optionalField("email", string().minLength(1).maxLength(100)),
            optionalField("name", string().minLength(1).maxLength(100))
    ).map((email, name) -> new UpdateProfileInput(
            email.map(EmailAddress::of).orElse(null),
            name.map(UserName::of).orElse(null)));

    public record UpdateProfileInput(EmailAddress email, UserName name) {}
}
