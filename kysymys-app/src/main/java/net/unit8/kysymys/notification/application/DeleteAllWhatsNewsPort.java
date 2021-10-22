package net.unit8.kysymys.notification.application;

import net.unit8.kysymys.notification.domain.WhatsNewId;
import net.unit8.kysymys.user.domain.UserId;

import java.util.List;

public interface DeleteAllWhatsNewsPort {
    void deleteAll(UserId userId, List<WhatsNewId> whatsNewIds);
}
