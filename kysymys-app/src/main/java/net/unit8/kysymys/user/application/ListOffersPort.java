package net.unit8.kysymys.user.application;

import net.unit8.kysymys.user.domain.Offer;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.data.domain.Page;

public interface ListOffersPort {
    Page<Offer> listByTargetUser(UserId targetUserId, int page, int size);
}
