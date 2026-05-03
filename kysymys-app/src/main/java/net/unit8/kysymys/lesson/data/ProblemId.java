package net.unit8.kysymys.lesson.data;

import java.util.Objects;

public record ProblemId(String value) {
    public ProblemId {
        Objects.requireNonNull(value, "value");
        if (value.length() != 21) {
            throw new IllegalArgumentException("ProblemId must be 21 chars (nanoid)");
        }
    }
    public static ProblemId of(String value) { return new ProblemId(value); }
    public static ProblemId newId() { return new ProblemId(IdGenerator.newId()); }
}
