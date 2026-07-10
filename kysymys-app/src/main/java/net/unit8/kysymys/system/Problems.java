package net.unit8.kysymys.system;

import kotowari.restful.data.Problem;
import net.unit8.raoh.Issues;

import java.util.List;

/**
 * Bridges Raoh validation failures to kotowari-restful's RFC 9457
 * {@code application/problem+json}.
 *
 * <p>Every {@code @Decision(MALFORMED)} needs to turn a Raoh {@code Err}'s
 * accumulated {@link Issues} into a {@link Problem}. Keeping that mapping here —
 * context-neutral, taking {@link Issues} rather than a {@code Result} — means no
 * resource has to reach across bounded contexts for it or downcast a sealed
 * {@code Result} to {@code Err}.
 */
public final class Problems {
    private Problems() {}

    /**
     * Renders accumulated validation issues as a problem+json body, one
     * {@link Problem.Violation} per issue, keyed by its JSON Pointer path.
     */
    public static Problem of(Issues issues) {
        List<Problem.Violation> violations = issues.asList().stream()
                .map(i -> new Problem.Violation(i.path().toJsonPointer(), i.code(), i.message()))
                .toList();
        return Problem.fromViolationList(violations);
    }
}
