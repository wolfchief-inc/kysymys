package net.unit8.kysymys.lesson.data;

import java.util.Objects;

public record CommentId(String value) {
    public CommentId {
        Objects.requireNonNull(value, "value");
        if (value.length() != 21) {
            throw new IllegalArgumentException("CommentId must be 21 chars (nanoid)");
        }
    }
    public static CommentId of(String value) { return new CommentId(value); }
    public static CommentId newId() { return new CommentId(IdGenerator.newId()); }
}
