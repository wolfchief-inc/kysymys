package net.unit8.kysymys.lesson.data;

import java.util.Objects;

public record AnswerId(String value) {
    public AnswerId {
        Objects.requireNonNull(value, "value");
        if (value.length() != 21) {
            throw new IllegalArgumentException("AnswerId must be 21 chars (nanoid)");
        }
    }
    public static AnswerId of(String value) { return new AnswerId(value); }
    public static AnswerId newId() { return new AnswerId(IdGenerator.newId()); }
}
