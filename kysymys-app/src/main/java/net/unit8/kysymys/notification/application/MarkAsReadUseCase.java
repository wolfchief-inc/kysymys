package net.unit8.kysymys.notification.application;

import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

public interface MarkAsReadUseCase {
    MarkedAsReadEvent handle(MarkAsReadCommand command);

    @Value
    class MarkAsReadCommand {
        String userId;
        List<String> whatsNewIds;
    }

    @Value
    class MarkedAsReadEvent {
        LocalDateTime occurredAt;
    }
}
