package net.unit8.kysymys.notification.application;

import lombok.Value;

import java.io.Serializable;
import java.util.Map;

@Value
public class SendMailEvent implements Serializable {
    String[] to;
    String subject;
    String templatePath;
    Map<String, Object> params;
}
