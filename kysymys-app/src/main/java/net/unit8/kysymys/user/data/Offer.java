package net.unit8.kysymys.user.data;

import java.time.LocalDateTime;
import java.util.Objects;

public record Offer(
        OfferId id,
        UserId offeringUserId,
        UserId targetUserId,
        LocalDateTime offeredAt
) {
    public Offer {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(offeringUserId, "offeringUserId");
        Objects.requireNonNull(targetUserId, "targetUserId");
        Objects.requireNonNull(offeredAt, "offeredAt");
    }
}
