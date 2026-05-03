package net.unit8.kysymys.lesson.data;

import java.util.Objects;

public record ProblemEventId(String value) {
    public ProblemEventId {
        Objects.requireNonNull(value, "value");
        if (value.length() != 21) {
            throw new IllegalArgumentException("ProblemEventId must be 21 chars (nanoid)");
        }
    }
    public static ProblemEventId of(String value) { return new ProblemEventId(value); }
    public static ProblemEventId newId() { return new ProblemEventId(IdGenerator.newId()); }
}
