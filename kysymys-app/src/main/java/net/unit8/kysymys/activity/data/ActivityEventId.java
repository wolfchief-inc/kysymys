package net.unit8.kysymys.activity.data;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

import java.util.Objects;

/** 21-character nanoid identifier for an {@link ActivityEvent}. */
public record ActivityEventId(String value) {
    public ActivityEventId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("ActivityEventId value must not be blank");
        }
    }

    public static ActivityEventId newId() {
        return new ActivityEventId(NanoIdUtils.randomNanoId());
    }
}
