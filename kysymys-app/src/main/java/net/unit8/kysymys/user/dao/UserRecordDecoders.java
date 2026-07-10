package net.unit8.kysymys.user.dao;

import net.unit8.kysymys.user.data.Offer;
import net.unit8.kysymys.user.data.OfferId;
import net.unit8.kysymys.user.data.UserId;
import net.unit8.raoh.decode.Decoder;
import org.jooq.Record;

import static net.unit8.raoh.decode.ObjectDecoders.dateTime;
import static net.unit8.raoh.decode.ObjectDecoders.string;
import static net.unit8.raoh.jooq.JooqRecordDecoders.combine;
import static net.unit8.raoh.jooq.JooqRecordDecoders.field;

/**
 * raoh-jooq decoders mapping User-context rows into aggregates.
 *
 * <p>{@code User} itself is not decoded here: its roles come from a second
 * {@code user_roles} query, so {@link UserDao} keeps that assembly.
 */
public final class UserRecordDecoders {
    private UserRecordDecoders() {}

    public static final Decoder<Record, Offer> OFFER = combine(
            field("id", string()).map(OfferId::of),
            field("offering_user_id", string()).map(UserId::of),
            field("target_user_id", string()).map(UserId::of),
            field("offered_at", dateTime())
    ).map(Offer::new);
}
