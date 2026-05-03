package net.unit8.kysymys.lesson.data;

import java.util.Objects;

public record ProblemName(String value) {
    public ProblemName {
        Objects.requireNonNull(value, "value");
        String trimmed = value.strip();
        if (trimmed.isEmpty() || trimmed.length() > 100) {
            throw new IllegalArgumentException("ProblemName must be 1..100 chars");
        }
    }
}
