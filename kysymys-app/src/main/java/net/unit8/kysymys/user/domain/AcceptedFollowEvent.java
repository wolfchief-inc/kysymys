package net.unit8.kysymys.user.domain;

import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

@Value
public class AcceptedFollowEvent implements Serializable {
    LocalDateTime occurredAt;
}
