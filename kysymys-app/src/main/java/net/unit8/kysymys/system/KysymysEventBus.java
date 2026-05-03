package net.unit8.kysymys.system;

import enkan.component.ComponentLifecycle;
import enkan.component.SystemComponent;
import net.unit8.kysymys.events.KysymysEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * In-memory pub/sub bus for cross-context domain events. Producers (Lesson,
 * User) call {@link #publish}; consumers (Notification) register handlers
 * via {@link #subscribe} during application startup.
 *
 * <p>Dispatch is synchronous on the publisher's thread. Handler exceptions
 * are isolated so a failure in one subscriber doesn't break others.
 */
public class KysymysEventBus extends SystemComponent<KysymysEventBus> {

    private final ConcurrentHashMap<Class<? extends KysymysEvent>,
            CopyOnWriteArrayList<Consumer<KysymysEvent>>> subscribers = new ConcurrentHashMap<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends KysymysEvent> void subscribe(Class<T> type, Consumer<T> handler) {
        subscribers.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>())
                .add((Consumer) handler);
    }

    public <T extends KysymysEvent> void publish(T event) {
        List<Consumer<KysymysEvent>> handlers = subscribers.get(event.getClass());
        if (handlers == null) return;
        for (Consumer<KysymysEvent> handler : new ArrayList<>(handlers)) {
            try {
                handler.accept(event);
            } catch (RuntimeException ex) {
                // Swallow per-handler errors so peers keep running.
                System.err.println("KysymysEventBus handler threw: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    @Override
    protected ComponentLifecycle<KysymysEventBus> lifecycle() {
        return new ComponentLifecycle<>() {
            @Override public void start(KysymysEventBus component) { /* no-op */ }
            @Override public void stop(KysymysEventBus component) {
                component.subscribers.clear();
            }
        };
    }
}
