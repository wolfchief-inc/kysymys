package net.unit8.kysymys.user.domain;

import lombok.Value;

@Value
public class OfferedToFollowEvent {
    User targetUser;
}
