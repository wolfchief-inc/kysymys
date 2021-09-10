package net.unit8.kysymys.notification.application.impl;

import net.unit8.kysymys.notification.application.GetWhatsNewsQuery;
import net.unit8.kysymys.notification.application.GetWhatsNewsUseCase;
import net.unit8.kysymys.notification.domain.WhatsNew;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
class GetWhatsNewsUseCaseImpl implements GetWhatsNewsUseCase {
    @Override
    public Page<WhatsNew> handle(GetWhatsNewsQuery query) {
        return null;
    }
}
