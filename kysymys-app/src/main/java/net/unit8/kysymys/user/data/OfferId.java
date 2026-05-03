package net.unit8.kysymys.user.data;

import net.unit8.kysymys.lesson.data.IdGenerator;

import java.util.Objects;

public record OfferId(String value) {
    public OfferId {
        Objects.requireNonNull(value, "value");
        if (value.length() != 21) {
            throw new IllegalArgumentException("OfferId must be 21 chars (nanoid)");
        }
    }

    public static OfferId of(String value) { return new OfferId(value); }
    public static OfferId newId() { return new OfferId(IdGenerator.newId()); }
}
