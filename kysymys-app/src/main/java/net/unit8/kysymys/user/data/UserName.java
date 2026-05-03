package net.unit8.kysymys.user.data;

import java.util.Objects;

public record UserName(String value) {
    public UserName {
        Objects.requireNonNull(value, "value");
        String trimmed = value.strip();
        if (trimmed.isEmpty() || trimmed.length() > 100) {
            throw new IllegalArgumentException("UserName must be 1..100 chars");
        }
    }

    public static UserName of(String value) {
        return new UserName(value);
    }
}
