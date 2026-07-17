package net.unit8.kysymys.system;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.data.Extendable;
import enkan.web.data.HttpRequest;
import enkan.web.data.HttpResponse;
import enkan.web.middleware.WebMiddleware;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

/**
 * Middleware that exposes the {@link KysymysEventBus} singleton to downstream
 * resources via the request's extension map. {@code EventBusInjector} reads
 * it back when constructing {@code @Decision} method arguments.
 */
@Middleware(name = "eventBusBind")
public class EventBusBindMiddleware implements WebMiddleware {

    @Inject
    KysymysEventBus eventBus;

    @Override
    public <NNREQ, NNRES> @Nullable HttpResponse handle(HttpRequest request,
                                                        MiddlewareChain<HttpRequest, HttpResponse, NNREQ, NNRES> chain) {
        Object proxy = request;
        if (proxy instanceof Extendable e) {
            e.setExtension("kysymysEventBus", eventBus);
        }
        return chain.next(request);
    }

    public KysymysEventBus getEventBus() {
        return eventBus;
    }
}
