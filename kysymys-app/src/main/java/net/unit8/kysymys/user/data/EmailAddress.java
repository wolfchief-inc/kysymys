package net.unit8.kysymys.user.data;

import java.util.Objects;

public record EmailAddress(String value) {
    public EmailAddress {
        Objects.requireNonNull(value, "value");
        String trimmed = value.strip();
        if (trimmed.isEmpty() || trimmed.length() > 100) {
            throw new IllegalArgumentException("EmailAddress must be 1..100 chars");
        }
        if (!trimmed.contains("@")) {
            throw new IllegalArgumentException("EmailAddress must contain '@'");
        }
    }

    public static EmailAddress of(String value) {
        return new EmailAddress(value);
    }
}
