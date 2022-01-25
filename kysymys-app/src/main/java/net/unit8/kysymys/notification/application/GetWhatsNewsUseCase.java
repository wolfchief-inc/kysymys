package net.unit8.kysymys.notification.application;

import lombok.Value;
import net.unit8.kysymys.notification.domain.WhatsNew;
import org.springframework.data.domain.Page;

public interface GetWhatsNewsUseCase {
    Page<WhatsNew> handle(GetWhatsNewsQuery query);

    @Value
    class GetWhatsNewsQuery {
        String userId;
        int size;
    }

}
