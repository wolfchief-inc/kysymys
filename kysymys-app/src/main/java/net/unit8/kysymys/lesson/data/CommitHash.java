package net.unit8.kysymys.lesson.data;

import java.util.Objects;
import java.util.regex.Pattern;

public record CommitHash(String value) {
    private static final Pattern HEX40 = Pattern.compile("^[0-9a-fA-F]{40}$");

    public CommitHash {
        Objects.requireNonNull(value, "value");
        if (!HEX40.matcher(value).matches()) {
            throw new IllegalArgumentException("CommitHash must be 40 hex chars");
        }
    }
}
