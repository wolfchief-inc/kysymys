package net.unit8.kysymys.notification.data;

import net.unit8.kysymys.user.data.UserId;

import java.util.Objects;

public record UnreadWhatsNew(WhatsNewId id, WhatsNewId whatsNewId, UserId userId) {
    public UnreadWhatsNew {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(whatsNewId, "whatsNewId");
        Objects.requireNonNull(userId, "userId");
    }
}
