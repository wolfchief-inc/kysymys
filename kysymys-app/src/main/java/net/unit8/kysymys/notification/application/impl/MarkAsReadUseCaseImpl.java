package net.unit8.kysymys.notification.application.impl;

import net.unit8.kysymys.notification.application.DeleteAllWhatsNewsPort;
import net.unit8.kysymys.notification.application.MarkAsReadCommand;
import net.unit8.kysymys.notification.application.MarkAsReadUseCase;
import net.unit8.kysymys.notification.domain.MarkedAsReadEvent;
import net.unit8.kysymys.notification.domain.WhatsNewId;
import net.unit8.kysymys.share.application.CurrentDateTimePort;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.stream.Collectors;

@Component
class MarkAsReadUseCaseImpl implements MarkAsReadUseCase {
    private final DeleteAllWhatsNewsPort deleteAllWhatsNewsPort;
    private final CurrentDateTimePort currentDateTimePort;
    private final TransactionTemplate tx;

    public MarkAsReadUseCaseImpl(DeleteAllWhatsNewsPort deleteAllWhatsNewsPort, CurrentDateTimePort currentDateTimePort, TransactionTemplate tx) {
        this.deleteAllWhatsNewsPort = deleteAllWhatsNewsPort;
        this.currentDateTimePort = currentDateTimePort;
        this.tx = tx;
    }

    @Override
    public MarkedAsReadEvent handle(MarkAsReadCommand command) {
        return tx.execute(status -> {
            deleteAllWhatsNewsPort.deleteAll(UserId.of(command.getUserId()),
                    command.getWhatsNewIds().stream()
                            .map(WhatsNewId::of)
                            .collect(Collectors.toList()));
            return new MarkedAsReadEvent(currentDateTimePort.now());
        });
    }
}
