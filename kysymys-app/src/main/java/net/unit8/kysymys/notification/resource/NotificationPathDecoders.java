package net.unit8.kysymys.notification.resource;

import net.unit8.kysymys.notification.data.WhatsNewId;
import net.unit8.raoh.decode.Decoder;

import static net.unit8.raoh.decode.ObjectDecoders.string;

/**
 * Raoh decoders for the Notification context's path parameters.
 *
 * <p>Decodes the raw {@code :id} string from the URL into a {@link WhatsNewId}
 * via {@link net.unit8.raoh.decode.ObjectDecoders#string()}; a malformed id
 * decodes to an {@code Err} that the resource turns into a 404.
 */
public final class NotificationPathDecoders {
    private NotificationPathDecoders() {}

    /** {@code :id} of a WhatsNew notification — a 21-char nanoid. */
    public static final Decoder<Object, WhatsNewId> WHATS_NEW_ID =
            string().fixedLength(21).map(WhatsNewId::of);
}
