package net.unit8.kysymys.notification.application;

import net.unit8.kysymys.notification.domain.WhatsNew;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.data.domain.Page;

public interface GetWhatsNewsUseCase {
    Page<WhatsNew> handle(GetWhatsNewsQuery query);
}
