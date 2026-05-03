package net.unit8.kysymys.user.resource;

import net.unit8.kysymys.user.data.Role;
import net.unit8.kysymys.user.data.User;

import java.util.LinkedHashMap;
import java.util.Map;

public final class UserJsonEncoders {
    private UserJsonEncoders() {}

    public static Map<String, Object> encode(User u) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", u.id().value());
        body.put("email", u.email().value());
        body.put("name", u.name().value());
        body.put("roles", u.roles().values().stream().map(Role::name).toList());
        return body;
    }
}
