package net.unit8.kysymys.user.dao;

import net.unit8.kysymys.user.data.Offer;
import net.unit8.kysymys.user.data.OfferId;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class OfferDao {
    private static final Field<String> ID = field("id", String.class);
    private static final Field<String> OFFERING_USER_ID = field("offering_user_id", String.class);
    private static final Field<String> TARGET_USER_ID = field("target_user_id", String.class);
    private static final Field<LocalDateTime> OFFERED_AT = field("offered_at", LocalDateTime.class);

    private final DSLContext dsl;

    public OfferDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void insert(Offer offer) {
        dsl.insertInto(table("offers"), ID, OFFERING_USER_ID, TARGET_USER_ID, OFFERED_AT)
                .values(offer.id().value(),
                        offer.offeringUserId().value(),
                        offer.targetUserId().value(),
                        offer.offeredAt())
                .execute();
    }

    public Optional<Offer> findById(OfferId id) {
        Record rec = dsl.select(ID, OFFERING_USER_ID, TARGET_USER_ID, OFFERED_AT)
                .from(table("offers"))
                .where(ID.eq(id.value()))
                .fetchOne();
        return Optional.ofNullable(rec).map(r -> UserRecordDecoders.OFFER.decode(r).getOrThrow());
    }

    public List<Offer> listByTarget(UserId targetUserId) {
        return dsl.select(ID, OFFERING_USER_ID, TARGET_USER_ID, OFFERED_AT)
                .from(table("offers"))
                .where(TARGET_USER_ID.eq(targetUserId.value()))
                .orderBy(OFFERED_AT.asc())
                .fetch(r -> UserRecordDecoders.OFFER.decode(r).getOrThrow());
    }

    public boolean alreadyExists(UserId offeringUserId, UserId targetUserId) {
        return dsl.selectCount().from(table("offers"))
                .where(OFFERING_USER_ID.eq(offeringUserId.value()))
                .and(TARGET_USER_ID.eq(targetUserId.value()))
                .fetchOne(0, Long.class) > 0;
    }

    public void delete(OfferId id) {
        dsl.deleteFrom(table("offers")).where(ID.eq(id.value())).execute();
    }
}
