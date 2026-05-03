package net.unit8.kysymys.lesson.data;

import java.util.Objects;

public record Description(String value) {
    public Description {
        Objects.requireNonNull(value, "value");
        String trimmed = value.strip();
        if (trimmed.isEmpty() || trimmed.length() > 4000) {
            throw new IllegalArgumentException("Description must be 1..4000 chars");
        }
    }
}
