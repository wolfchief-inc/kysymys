package net.unit8.kysymys.notification.application;

import net.unit8.kysymys.notification.domain.TemplatePath;
import net.unit8.kysymys.notification.domain.WhatsNew;
import net.unit8.kysymys.notification.domain.WhatsNewId;
import net.unit8.kysymys.share.application.CurrentDateTimePort;
import net.unit8.kysymys.share.application.GenerateCursorPort;
import net.unit8.kysymys.user.domain.OfferedToFollowEvent;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;

public class WhatsNewEventListener {
    private final SaveWhatsNewPort saveWhatsNewPort;
    private final GenerateCursorPort generateCursorPort;
    private final CurrentDateTimePort currentDateTimePort;
    private final TransactionTemplate tx;

    public WhatsNewEventListener(SaveWhatsNewPort saveWhatsNewPort, GenerateCursorPort generateCursorPort, CurrentDateTimePort currentDateTimePort, TransactionTemplate tx) {
        this.saveWhatsNewPort = saveWhatsNewPort;
        this.generateCursorPort = generateCursorPort;
        this.currentDateTimePort = currentDateTimePort;
        this.tx = tx;
    }

    @EventListener
    public void onOfferedToFollowEvent(OfferedToFollowEvent event) {
        WhatsNew whatsNew = WhatsNew.of(WhatsNewId.of(generateCursorPort.generateId()),
                UserId.of(event.getTargetUserId()),
                TemplatePath.of("message/offeredToFollowEvent"),
                Map.of("offeringUserName", event.getOfferingUserName()),
                currentDateTimePort.now());
        tx.execute(status -> {
            saveWhatsNewPort.save(whatsNew);
            return null;
        });
    }
}
