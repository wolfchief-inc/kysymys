package net.unit8.kysymys.notification.resource;

import net.unit8.kysymys.notification.data.WhatsNew;
import net.unit8.raoh.encode.Encoder;

import java.util.Map;

import static net.unit8.raoh.encode.MapEncoders.object;
import static net.unit8.raoh.encode.MapEncoders.property;
import static net.unit8.raoh.encode.ObjectEncoders.string;

/**
 * JSON response shapes for the Notification context, built with raoh-encode.
 *
 * <p>{@code unread} is not part of {@link WhatsNew}, so it is appended to the
 * encoded base map. {@code params} is an already-structured map, passed through
 * with an identity value encoder.
 */
public final class NotificationJsonEncoders {
    private NotificationJsonEncoders() {}

    private static final Encoder<WhatsNew, Map<String, Object>> WHATS_NEW = object(
            property("id", w -> w.id().value(), string()),
            property("templatePath", w -> w.templatePath().value(), string()),
            property("params", w -> w.params(), m -> m),
            property("postedAt", w -> w.postedAt().toString(), string()));

    public static Map<String, Object> encodeWhatsNew(WhatsNew w, boolean unread) {
        Map<String, Object> body = WHATS_NEW.encode(w);
        body.put("unread", unread);
        return body;
    }
}
