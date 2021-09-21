package net.unit8.kysymys.notification.application;

import net.unit8.kysymys.config.NotificationConfig;
import net.unit8.kysymys.notification.domain.MailMeta;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class SendMailEventListener {
    private final SendMailPort sendMailPort;
    private final NotificationConfig notificationConfig;

    public SendMailEventListener(SendMailPort sendMailPort, NotificationConfig notificationConfig) {
        this.sendMailPort = sendMailPort;
        this.notificationConfig = notificationConfig;
    }

    @EventListener
    @Async
    public void handle(SendMailEvent event) {
        MailMeta mailMeta = MailMeta.builder()
                .from(notificationConfig.getFromAddress())
                .to(event.getTo())
                .subject(event.getSubject())
                .build();
        sendMailPort.sendWithTemplate(mailMeta, event.getTemplatePath(), event.getParams());
    }
}
