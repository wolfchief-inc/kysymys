package net.unit8.kysymys.user.data;

import java.util.Objects;

public record User(
        UserId id,
        EmailAddress email,
        UserName name,
        Roles roles
) {
    public User {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(email, "email");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(roles, "roles");
    }
}
