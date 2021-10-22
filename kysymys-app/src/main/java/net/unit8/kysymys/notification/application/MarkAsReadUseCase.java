package net.unit8.kysymys.notification.application;

import net.unit8.kysymys.notification.domain.MarkedAsReadEvent;

public interface MarkAsReadUseCase {
    MarkedAsReadEvent handle(MarkAsReadCommand command);
}
