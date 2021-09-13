package net.unit8.kysymys.user.application;

import net.unit8.kysymys.user.domain.UserId;

public interface HasAlreadyOfferedPort {
    boolean hasAlreadyOffered(UserId offeringUserId, UserId targetUserId);
}
