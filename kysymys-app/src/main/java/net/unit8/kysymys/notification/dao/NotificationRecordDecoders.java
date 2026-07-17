package net.unit8.kysymys.notification.dao;

import net.unit8.kysymys.notification.data.TemplatePath;
import net.unit8.kysymys.notification.data.WhatsNew;
import net.unit8.kysymys.notification.data.WhatsNewId;
import net.unit8.kysymys.user.data.UserId;
import net.unit8.raoh.decode.Decoder;
import org.jooq.Record;

import static net.unit8.raoh.decode.ObjectDecoders.dateTime;
import static net.unit8.raoh.decode.ObjectDecoders.string;
import static net.unit8.raoh.jooq.JooqRecordDecoders.combine;
import static net.unit8.raoh.jooq.JooqRecordDecoders.field;

/**
 * raoh-jooq decoders mapping Notification-context rows into aggregates.
 *
 * <p>The {@code params} column stores JSON; it is decoded through
 * {@link WhatsNewDao#deserialiseParams} so the read path stays symmetric with the
 * write path's serialisation.
 */
public final class NotificationRecordDecoders {
    private NotificationRecordDecoders() {}

    public static final Decoder<Record, WhatsNew> WHATS_NEW = combine(
            field("id", string()).map(WhatsNewId::of),
            field("user_id", string()).map(UserId::of),
            field("template_path", string()).map(TemplatePath::of),
            field("params", string()).map(WhatsNewDao::deserialiseParams),
            field("posted_at", dateTime())
    ).map(WhatsNew::new);
}
