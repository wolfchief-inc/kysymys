package net.unit8.kysymys.inject;

import enkan.data.Extendable;
import enkan.web.data.HttpRequest;
import kotowari.inject.ParameterInjector;
import net.unit8.kysymys.system.KysymysEventBus;
import org.jspecify.annotations.Nullable;

/**
 * Injects the singleton {@link KysymysEventBus} into resource methods.
 * Reads it from the request extension {@code "kysymysEventBus"} which the
 * {@code EventBusBindMiddleware} populates from a {@code @Inject} field.
 */
public class EventBusInjector implements ParameterInjector<KysymysEventBus> {

    @Override
    public String getName() {
        return "eventBus";
    }

    @Override
    public boolean isApplicable(Class<?> type) {
        return KysymysEventBus.class.isAssignableFrom(type);
    }

    @Override
    public @Nullable KysymysEventBus getInjectObject(HttpRequest request) {
        Object proxy = request;
        if (proxy instanceof Extendable e) {
            return e.getExtension("kysymysEventBus");
        }
        return null;
    }
}
