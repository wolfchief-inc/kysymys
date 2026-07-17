package net.unit8.kysymys.system;

import kotowari.restful.data.ContextKey;
import kotowari.restful.data.RestContext;

import java.util.Optional;

/**
 * Small combinators over kotowari-restful's {@link RestContext}.
 *
 * <p>{@code RestContext.put} returns {@code void}, so the recurring decision shape
 * — produce a value, remember it for later decisions, and report whether there was
 * one — cannot be written as a single fluent expression at the call site.
 * {@link #stash} names that operation once so every resource can express it
 * declaratively.
 */
public final class RestContexts {
    private RestContexts() {}

    /**
     * Stores {@code value} under {@code key} when present and reports its presence.
     *
     * <p>The canonical body of a decision that resolves an entity: for
     * {@code @Decision(EXISTS)} a present value means it exists (else 404); for a
     * {@code POST}/{@code PUT} a present value means the entity was created or
     * updated (else the action failed). Either way the value becomes available to
     * downstream decisions via {@code key}.
     */
    public static <T> boolean stash(RestContext context, ContextKey<T> key, Optional<T> value) {
        value.ifPresent(v -> context.put(key, v));
        return value.isPresent();
    }
}
