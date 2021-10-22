package net.unit8.kysymys.notification.domain;

import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

@Value
public class MarkedAsReadEvent implements Serializable {
    LocalDateTime occurredAt;
}
