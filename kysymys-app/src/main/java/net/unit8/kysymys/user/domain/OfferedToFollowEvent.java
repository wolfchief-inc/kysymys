package net.unit8.kysymys.user.domain;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class OfferedToFollowEvent {
    String targetUserId;
    String targetUserName;
    String targetUserEmail;
    String offeringUserId;
    String offeringUserName;
    LocalDateTime occurredAt;
}
