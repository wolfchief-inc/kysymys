package net.unit8.kysymys.user.resource;

import net.unit8.kysymys.user.data.Offer;
import net.unit8.kysymys.user.data.Role;
import net.unit8.kysymys.user.data.User;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JSON response shapes for the User context.
 */
public final class UserJsonEncoders {
    private UserJsonEncoders() {}

    public static Map<String, Object> encodeUser(User u) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", u.id().value());
        body.put("email", u.email().value());
        body.put("name", u.name().value());
        body.put("roles", u.roles().values().stream().map(Role::name).toList());
        return body;
    }

    public static Map<String, Object> encodeOffer(Offer o) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", o.id().value());
        body.put("offeringUserId", o.offeringUserId().value());
        body.put("targetUserId", o.targetUserId().value());
        body.put("offeredAt", o.offeredAt().toString());
        return body;
    }
}
