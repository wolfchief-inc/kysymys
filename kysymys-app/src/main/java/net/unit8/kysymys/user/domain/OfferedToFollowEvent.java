package net.unit8.kysymys.user.domain;

import lombok.Value;

@Value
public class OfferedToFollowEvent {
    String targetUserId;
    String targetUserName;
    String targetUserEmail;
    String offeringUserId;
    String offeringUserName;
}
