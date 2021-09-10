package net.unit8.kysymys.notification.application;

import net.unit8.kysymys.notification.domain.MailMeta;
import net.unit8.kysymys.user.domain.OfferedToFollowEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SendMailEventListener {
    private SendMailPort sendMailPort;

    @EventListener
    public void handle(OfferedToFollowEvent event) {
        Map<String, Object> params = new HashMap<>();
        MailMeta mailMeta = MailMeta.builder().build();
        sendMailPort.sendWithTemplate(mailMeta, "", params);
    }
}
