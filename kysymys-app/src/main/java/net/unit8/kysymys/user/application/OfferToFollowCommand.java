package net.unit8.kysymys.user.application;

import lombok.Value;

@Value
public class OfferToFollowCommand {
    String offeringUserId;
    String targetUserId;
}
