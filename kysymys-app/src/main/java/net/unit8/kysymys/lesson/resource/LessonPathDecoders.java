package net.unit8.kysymys.lesson.resource;

import net.unit8.kysymys.lesson.data.AnswerId;
import net.unit8.kysymys.lesson.data.ProblemId;
import net.unit8.raoh.decode.Decoder;

import static net.unit8.raoh.decode.ObjectDecoders.string;

/**
 * Raoh decoders for the Lesson context's path parameters.
 *
 * <p>Counterpart to {@link LessonJsonDecoders}: that class decodes JSON request
 * bodies ({@code Decoder<JsonNode, T>}); this one decodes the raw {@code :id}
 * strings Kotowari extracts from the URL ({@code Decoder<Object, T>}). Both are
 * built from the same Raoh vocabulary — here {@link net.unit8.raoh.decode.ObjectDecoders#string()},
 * the factory for scalar boundary values. A malformed id decodes to an
 * {@code Err} that resources turn into a 404, instead of catching
 * {@code IllegalArgumentException} from the value-object constructor.
 */
public final class LessonPathDecoders {
    private LessonPathDecoders() {}

    /** {@code :id} of an Answer — a 21-char nanoid. */
    public static final Decoder<Object, AnswerId> ANSWER_ID =
            string().fixedLength(21).map(AnswerId::of);

    /** {@code :id} of a Problem — a 21-char nanoid. */
    public static final Decoder<Object, ProblemId> PROBLEM_ID =
            string().fixedLength(21).map(ProblemId::of);
}
