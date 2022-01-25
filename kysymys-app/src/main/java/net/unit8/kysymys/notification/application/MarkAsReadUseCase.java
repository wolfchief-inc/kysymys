package net.unit8.kysymys.notification.application;

import lombok.Value;
import net.unit8.kysymys.notification.domain.MarkedAsReadEvent;

import java.util.List;

public interface MarkAsReadUseCase {
    MarkedAsReadEvent handle(MarkAsReadCommand command);

    @Value
    class MarkAsReadCommand {
        String userId;
        List<String> whatsNewIds;
    }

}
