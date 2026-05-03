package net.unit8.kysymys.lesson.data;

import java.util.Objects;

public record ProblemLifecycleId(String value) {
    public ProblemLifecycleId {
        Objects.requireNonNull(value, "value");
        if (value.length() != 21) {
            throw new IllegalArgumentException("ProblemLifecycleId must be 21 chars (nanoid)");
        }
    }
    public static ProblemLifecycleId of(String value) { return new ProblemLifecycleId(value); }
    public static ProblemLifecycleId newId() { return new ProblemLifecycleId(IdGenerator.newId()); }
}
