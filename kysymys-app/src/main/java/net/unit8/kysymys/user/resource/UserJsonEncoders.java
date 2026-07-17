package net.unit8.kysymys.user.resource;

import net.unit8.kysymys.user.data.Offer;
import net.unit8.kysymys.user.data.User;
import net.unit8.raoh.encode.Encoder;

import java.util.Map;

import static net.unit8.raoh.encode.MapEncoders.list;
import static net.unit8.raoh.encode.MapEncoders.object;
import static net.unit8.raoh.encode.MapEncoders.property;
import static net.unit8.raoh.encode.ObjectEncoders.string;

/**
 * JSON response shapes for the User context, built with raoh-encode — the mirror
 * of {@code UserJsonDecoders} on the request side.
 */
public final class UserJsonEncoders {
    private UserJsonEncoders() {}

    private static final Encoder<User, Map<String, Object>> USER = object(
            property("id", u -> u.id().value(), string()),
            property("email", u -> u.email().value(), string()),
            property("name", u -> u.name().value(), string()),
            property("roles", u -> u.roles().values().stream().map(r -> r.name()).toList(), list(string())));

    private static final Encoder<Offer, Map<String, Object>> OFFER = object(
            property("id", o -> o.id().value(), string()),
            property("offeringUserId", o -> o.offeringUserId().value(), string()),
            property("targetUserId", o -> o.targetUserId().value(), string()),
            property("offeredAt", o -> o.offeredAt().toString(), string()));

    public static Map<String, Object> encodeUser(User u) {
        return USER.encode(u);
    }

    public static Map<String, Object> encodeOffer(Offer o) {
        return OFFER.encode(o);
    }
}
