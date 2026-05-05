package net.unit8.kysymys.notification.resource;

import net.unit8.kysymys.notification.data.WhatsNew;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JSON response shapes for the Notification context.
 */
public final class NotificationJsonEncoders {
    private NotificationJsonEncoders() {}

    public static Map<String, Object> encodeWhatsNew(WhatsNew w, boolean unread) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", w.id().value());
        body.put("templatePath", w.templatePath().value());
        body.put("params", w.params());
        body.put("postedAt", w.postedAt().toString());
        body.put("unread", unread);
        return body;
    }
}
