package net.unit8.kysymys.user.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class OfferedToFollowEvent {
    String targetUserId;
    String targetUserName;
    String offeringUserId;
    String offeringUserName;
}
