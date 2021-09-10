package net.unit8.kysymys.user.application;

import lombok.Value;

import java.io.Serializable;

@Value
public class AcceptFollowCommand implements Serializable {
    String offerId;
    String acceptorId;
}
