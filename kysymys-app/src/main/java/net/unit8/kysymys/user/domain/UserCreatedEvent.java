package net.unit8.kysymys.user.domain;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class UserCreatedEvent {
    String userId;
    LocalDateTime occurredAt;
}
