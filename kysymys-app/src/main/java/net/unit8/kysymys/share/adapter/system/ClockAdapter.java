package net.unit8.kysymys.share.adapter.system;

import net.unit8.kysymys.share.application.CurrentDateTimePort;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
class ClockAdapter implements CurrentDateTimePort {
    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
