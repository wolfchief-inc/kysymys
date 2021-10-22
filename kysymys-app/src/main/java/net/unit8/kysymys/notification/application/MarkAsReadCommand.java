package net.unit8.kysymys.notification.application;

import lombok.Value;

import java.io.Serializable;
import java.util.List;

@Value
public class MarkAsReadCommand implements Serializable {
    String userId;
    List<String> whatsNewIds;
}
