package net.unit8.kysymys.notification.system;

import enkan.MiddlewareChain;
import enkan.annotation.Middleware;
import enkan.component.jooq.JooqProvider;
import enkan.web.data.HttpRequest;
import enkan.web.data.HttpResponse;
import enkan.web.middleware.WebMiddleware;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import net.unit8.kysymys.events.OfferedToFollowEvent;
import net.unit8.kysymys.events.SubmittedAnswerEvent;
import net.unit8.kysymys.notification.behavior.RecordWhatsNew;
import net.unit8.kysymys.system.KysymysEventBus;

/**
 * Pass-through middleware whose only purpose is to lazily wire
 * {@link RecordWhatsNew} into the {@link KysymysEventBus} once the system
 * has been started. Implemented as a middleware (instead of a separate
 * SystemComponent) so that field injection picks up the singleton bus and
 * the JooqProvider for free.
 */
@Middleware(name = "recordWhatsNewSubscriber")
public class RecordWhatsNewSubscriber implements WebMiddleware {

    @Inject
    KysymysEventBus eventBus;

    @Inject
    JooqProvider jooqProvider;

    @PostConstruct
    void init() {
        bus().subscribe(SubmittedAnswerEvent.class, e ->
                new RecordWhatsNew(jooqProvider.getDSLContext()).onSubmittedAnswer(e));
        bus().subscribe(OfferedToFollowEvent.class, e ->
                new RecordWhatsNew(jooqProvider.getDSLContext()).onOfferedToFollow(e));
    }

    private KysymysEventBus bus() {
        return eventBus;
    }

    @Override
    public <NNREQ, NNRES> HttpResponse handle(HttpRequest request,
                                              MiddlewareChain<HttpRequest, HttpResponse, NNREQ, NNRES> chain) {
        return chain.next(request);
    }
}
