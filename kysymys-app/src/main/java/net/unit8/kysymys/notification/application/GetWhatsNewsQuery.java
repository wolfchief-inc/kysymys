package net.unit8.kysymys.notification.application;

import lombok.Value;

@Value
public class GetWhatsNewsQuery {
    String userId;
    int size;
}
