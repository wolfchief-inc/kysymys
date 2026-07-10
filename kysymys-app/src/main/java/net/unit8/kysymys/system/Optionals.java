package net.unit8.kysymys.system;

import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Applicative combinators over {@link Optional}.
 *
 * <p>{@code Optional} only offers monadic {@code flatMap}, which reads as if the
 * second value <em>depends</em> on the first. When two optionals are independent —
 * two separate {@code RestContext} lookups, say — {@link #map2} combines them,
 * applying {@code f} only when both are present, so the call site reads "with both
 * values" instead of nesting one lookup inside the other.
 */
public final class Optionals {
    private Optionals() {}

    /**
     * Combines two independent optionals, applying {@code f} only when both are present.
     *
     * <p>The {@link Optional} analogue of Raoh's {@code Result.map2}.
     */
    public static <A, B, R> Optional<R> map2(Optional<A> a, Optional<B> b, BiFunction<A, B, R> f) {
        return a.flatMap(av -> b.map(bv -> f.apply(av, bv)));
    }
}
