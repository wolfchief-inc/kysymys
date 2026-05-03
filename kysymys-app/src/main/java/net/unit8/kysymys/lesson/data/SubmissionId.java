package net.unit8.kysymys.lesson.data;

import java.util.Objects;

public record SubmissionId(String value) {
    public SubmissionId {
        Objects.requireNonNull(value, "value");
        if (value.length() != 21) {
            throw new IllegalArgumentException("SubmissionId must be 21 chars (nanoid)");
        }
    }
    public static SubmissionId of(String value) { return new SubmissionId(value); }
    public static SubmissionId newId() { return new SubmissionId(IdGenerator.newId()); }
}
