package net.unit8.kysymys.system;

import net.unit8.kysymys.events.UserCreatedEvent;
import net.unit8.kysymys.user.data.UserId;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class KysymysEventBusTest {

    @Test
    void publishesToSubscribedHandler() {
        KysymysEventBus bus = new KysymysEventBus();
        AtomicInteger count = new AtomicInteger();
        bus.subscribe(UserCreatedEvent.class, event -> count.incrementAndGet());

        bus.publish(new UserCreatedEvent(
                UserId.of("a".repeat(21)), LocalDateTime.now()));

        assertThat(count.get()).isEqualTo(1);
    }

    @Test
    void publishWithNoSubscribersIsNoop() {
        KysymysEventBus bus = new KysymysEventBus();
        bus.publish(new UserCreatedEvent(
                UserId.of("a".repeat(21)), LocalDateTime.now()));
        // no exception is the assertion
    }

    @Test
    void multipleHandlersRunIndependentlyEvenIfOneThrows() {
        KysymysEventBus bus = new KysymysEventBus();
        AtomicInteger okCount = new AtomicInteger();
        bus.subscribe(UserCreatedEvent.class, event -> { throw new RuntimeException("boom"); });
        bus.subscribe(UserCreatedEvent.class, event -> okCount.incrementAndGet());

        bus.publish(new UserCreatedEvent(
                UserId.of("a".repeat(21)), LocalDateTime.now()));

        assertThat(okCount.get()).isEqualTo(1);
    }
}
