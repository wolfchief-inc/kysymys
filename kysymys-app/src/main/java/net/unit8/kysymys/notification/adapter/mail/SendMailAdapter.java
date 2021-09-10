package net.unit8.kysymys.notification.adapter.mail;

import net.unit8.kysymys.notification.application.SendMailPort;
import net.unit8.kysymys.notification.domain.MailMeta;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Component
class SendMailAdapter implements SendMailPort {
    private final TemplateEngine templateEngine;
    private final MailSender mailSender;

    SendMailAdapter(@Qualifier("mailTemplateEngine") TemplateEngine templateEngine,
                    MailSender mailSender) {
        this.templateEngine = templateEngine;
        this.mailSender = mailSender;
    }

    public void sendWithTemplate(MailMeta meta, String templatePath, Map<String, Object> params) {
        Context context = new Context();
        context.setVariables(params);
        String body = templateEngine.process(templatePath, context);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(meta.getFrom());
        msg.setTo(meta.getTo().toArray(String[]::new));
        msg.setSubject(meta.getSubject());
        msg.setText(body);
        mailSender.send(msg);
    }
}
