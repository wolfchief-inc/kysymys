package net.unit8.kysymys.notification.application;

import net.unit8.kysymys.notification.domain.MailMeta;

import java.util.Map;

public interface SendMailPort {
    void sendWithTemplate(MailMeta meta, String templatePath, Map<String, Object> params);
}
