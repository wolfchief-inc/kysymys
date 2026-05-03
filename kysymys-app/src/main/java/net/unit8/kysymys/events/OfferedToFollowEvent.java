package net.unit8.kysymys.events;

import net.unit8.kysymys.user.data.EmailAddress;
import net.unit8.kysymys.user.data.OfferId;
import net.unit8.kysymys.user.data.UserId;
import net.unit8.kysymys.user.data.UserName;

import java.time.LocalDateTime;
import java.util.Objects;

public record OfferedToFollowEvent(
        OfferId offerId,
        UserId offeringUserId,
        UserName offeringUserName,
        UserId targetUserId,
        UserName targetUserName,
        EmailAddress targetEmail,
        LocalDateTime occurredAt
) implements KysymysEvent {
    public OfferedToFollowEvent {
        Objects.requireNonNull(offerId, "offerId");
        Objects.requireNonNull(offeringUserId, "offeringUserId");
        Objects.requireNonNull(offeringUserName, "offeringUserName");
        Objects.requireNonNull(targetUserId, "targetUserId");
        Objects.requireNonNull(targetUserName, "targetUserName");
        Objects.requireNonNull(targetEmail, "targetEmail");
        Objects.requireNonNull(occurredAt, "occurredAt");
    }
}
