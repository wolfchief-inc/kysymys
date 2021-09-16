package net.unit8.kysymys.notification.application;

import lombok.Value;
import net.unit8.kysymys.user.domain.UserId;

@Value
public class GetWhatsNewsQuery {
    String userId;
    int size;
}
