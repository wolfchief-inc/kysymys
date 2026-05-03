package net.unit8.kysymys.notification.data;

import net.unit8.kysymys.lesson.data.IdGenerator;

import java.util.Objects;

public record WhatsNewId(String value) {
    public WhatsNewId {
        Objects.requireNonNull(value, "value");
        if (value.length() != 21) {
            throw new IllegalArgumentException("WhatsNewId must be 21 chars (nanoid)");
        }
    }
    public static WhatsNewId of(String value) { return new WhatsNewId(value); }
    public static WhatsNewId newId() { return new WhatsNewId(IdGenerator.newId()); }
}
