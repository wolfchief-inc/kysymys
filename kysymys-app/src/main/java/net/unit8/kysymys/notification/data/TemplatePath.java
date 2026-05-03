package net.unit8.kysymys.notification.data;

import java.util.Objects;

public record TemplatePath(String value) {
    public TemplatePath {
        Objects.requireNonNull(value, "value");
        String trimmed = value.strip();
        if (trimmed.isEmpty() || trimmed.length() > 100) {
            throw new IllegalArgumentException("TemplatePath must be 1..100 chars");
        }
    }
    public static TemplatePath of(String value) { return new TemplatePath(value); }
}
