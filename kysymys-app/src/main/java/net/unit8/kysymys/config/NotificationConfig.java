package net.unit8.kysymys.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "kysymys.notification")
public class NotificationConfig {
    String fromAddress;
}
