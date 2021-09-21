package net.unit8.kysymys.notification.application;

import net.unit8.kysymys.lesson.domain.SubmittedAnswerEvent;
import net.unit8.kysymys.notification.domain.TemplatePath;
import net.unit8.kysymys.notification.domain.WhatsNew;
import net.unit8.kysymys.notification.domain.WhatsNewId;
import net.unit8.kysymys.share.application.CurrentDateTimePort;
import net.unit8.kysymys.share.application.GenerateCursorPort;
import net.unit8.kysymys.user.domain.OfferedToFollowEvent;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class WhatsNewEventListener {
    private final SaveWhatsNewPort saveWhatsNewPort;
    private final GenerateCursorPort generateCursorPort;
    private final CurrentDateTimePort currentDateTimePort;
    private final MessageSource messageSource;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TransactionTemplate tx;

    public WhatsNewEventListener(SaveWhatsNewPort saveWhatsNewPort, GenerateCursorPort generateCursorPort, CurrentDateTimePort currentDateTimePort, MessageSource messageSource, ApplicationEventPublisher applicationEventPublisher, TransactionTemplate tx) {
        this.saveWhatsNewPort = saveWhatsNewPort;
        this.generateCursorPort = generateCursorPort;
        this.currentDateTimePort = currentDateTimePort;
        this.messageSource = messageSource;
        this.applicationEventPublisher = applicationEventPublisher;
        this.tx = tx;
    }

    @EventListener
    @Async
    public void onOfferedToFollow(OfferedToFollowEvent event) {
        Map<String, Object> params = Map.of("offeringUserName", event.getOfferingUserName());
        WhatsNew whatsNew = WhatsNew.of(WhatsNewId.of(generateCursorPort.generateId()),
                UserId.of(event.getTargetUserId()),
                TemplatePath.of("offeredToFollowEvent"),
                params,
                currentDateTimePort.now());
        tx.execute(status -> {
            saveWhatsNewPort.save(whatsNew);
            return null;
        });
        applicationEventPublisher.publishEvent(new SendMailEvent(
                new String[]{ event.getTargetUserEmail() },
                messageSource.getMessage("mail.subject.offered_to_follow", new Object[]{}, Locale.getDefault()),
                "offeredToFollowEvent",
                params
        ));
    }

    public void onSubmittedAnswer(SubmittedAnswerEvent event) {
        LocalDateTime now = currentDateTimePort.now();
        List<WhatsNew> whatsNews = event.getFollowers().stream().map(follower -> WhatsNew.of(WhatsNewId.of(generateCursorPort.generateId()),
                UserId.of(follower.getId()),
                TemplatePath.of("submittedAnswerEvent"),
                Map.of("answererId", event.getAnswererId(),
                        "answererName", event.getAnswererName()),
                now)).collect(Collectors.toList());
        tx.execute(status -> {
            whatsNews.forEach(saveWhatsNewPort::save);
            return null;
        });
    }
}
