package net.unit8.kysymys.user.resource;

import net.unit8.kysymys.user.data.OfferId;
import net.unit8.kysymys.user.data.UserId;
import net.unit8.raoh.decode.Decoder;

import static net.unit8.raoh.decode.ObjectDecoders.string;

/**
 * Raoh decoders for the User context's path parameters.
 *
 * <p>Counterpart to {@link UserJsonDecoders} on the body side. Decodes the raw
 * {@code :id} strings from the URL into typed identifiers via
 * {@link net.unit8.raoh.decode.ObjectDecoders#string()}; a malformed id becomes
 * an {@code Err} rather than an {@code IllegalArgumentException} caught for
 * control flow.
 *
 * <p>{@link #USER_ID} lives here because the User context owns {@code UserId};
 * other contexts (e.g. Avatar) reuse it rather than redeclaring the rule.
 */
public final class UserPathDecoders {
    private UserPathDecoders() {}

    /** {@code :id} of a User — a non-blank principal identifier. */
    public static final Decoder<Object, UserId> USER_ID =
            string().nonBlank().map(UserId::of);

    /** {@code :id} of a follow Offer — a 21-char nanoid. */
    public static final Decoder<Object, OfferId> OFFER_ID =
            string().fixedLength(21).map(OfferId::of);
}
