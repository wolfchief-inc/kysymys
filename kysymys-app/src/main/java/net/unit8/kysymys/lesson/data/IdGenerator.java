package net.unit8.kysymys.lesson.data;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

/** Generates 21-character nanoids for all Lesson aggregate ids. */
public final class IdGenerator {
    private IdGenerator() {}

    public static String newId() {
        return NanoIdUtils.randomNanoId();
    }
}
