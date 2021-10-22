package net.unit8.kysymys.notification.application.impl;

import net.unit8.kysymys.notification.application.GetWhatsNewsQuery;
import net.unit8.kysymys.notification.application.GetWhatsNewsUseCase;
import net.unit8.kysymys.notification.application.ListWhatsNewPort;
import net.unit8.kysymys.notification.domain.WhatsNew;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
class GetWhatsNewsUseCaseImpl implements GetWhatsNewsUseCase {
    private final ListWhatsNewPort listWhatsNewPort;

    GetWhatsNewsUseCaseImpl(ListWhatsNewPort listWhatsNewPort) {
        this.listWhatsNewPort = listWhatsNewPort;
    }

    @Override
    public Page<WhatsNew> handle(GetWhatsNewsQuery query) {
        return listWhatsNewPort.findLatestUnread(UserId.of(query.getUserId()), 5);
    }
}
