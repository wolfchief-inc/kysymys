package net.unit8.kysymys.notification.behavior;

import net.unit8.kysymys.events.OfferedToFollowEvent;
import net.unit8.kysymys.events.SubmittedAnswerEvent;
import net.unit8.kysymys.notification.dao.WhatsNewDao;
import net.unit8.kysymys.notification.data.TemplatePath;
import net.unit8.kysymys.notification.data.UnreadWhatsNew;
import net.unit8.kysymys.notification.data.WhatsNew;
import net.unit8.kysymys.notification.data.WhatsNewId;
import net.unit8.kysymys.system.KysymysEventBus;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;

import java.util.Map;

/**
 * Subscribes to cross-context events on the {@link KysymysEventBus} and
 * persists matching {@code whats_news} + {@code unread_whats_news} rows.
 *
 * <p>For {@link SubmittedAnswerEvent}: creates one WhatsNew per follower of
 * the answerer. For {@link OfferedToFollowEvent}: creates one WhatsNew for
 * the target user.
 */
public class RecordWhatsNew {
    private final DSLContext dsl;

    public RecordWhatsNew(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void wireSubscriptions(KysymysEventBus bus) {
        bus.subscribe(SubmittedAnswerEvent.class, this::onSubmittedAnswer);
        bus.subscribe(OfferedToFollowEvent.class, this::onOfferedToFollow);
    }

    public void onSubmittedAnswer(SubmittedAnswerEvent event) {
        Map<String, Object> params = Map.of(
                "answererId", event.answererId().value(),
                "answererName", event.answererName().value(),
                "problemId", event.problemId().value(),
                "problemName", event.problemName().value(),
                "answerId", event.answerId().value());
        for (UserId follower : event.followers()) {
            persist(follower, "submittedAnswer", params, event.occurredAt());
        }
    }

    public void onOfferedToFollow(OfferedToFollowEvent event) {
        Map<String, Object> params = Map.of(
                "offerId", event.offerId().value(),
                "offeringUserId", event.offeringUserId().value(),
                "offeringUserName", event.offeringUserName().value());
        persist(event.targetUserId(), "offeredToFollow", params, event.occurredAt());
    }

    private void persist(UserId targetUserId, String templateName,
                         Map<String, Object> params, java.time.LocalDateTime when) {
        WhatsNewId whatsNewId = WhatsNewId.newId();
        WhatsNew whatsNew = new WhatsNew(whatsNewId, targetUserId,
                TemplatePath.of(templateName), params, when);
        UnreadWhatsNew unread = new UnreadWhatsNew(WhatsNewId.newId(), whatsNewId, targetUserId);
        dsl.transaction(cfg -> {
            WhatsNewDao d = new WhatsNewDao(cfg.dsl());
            d.insert(whatsNew);
            d.insertUnread(unread);
        });
    }
}
