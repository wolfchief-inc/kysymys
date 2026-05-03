package net.unit8.kysymys.inject;

import enkan.data.Extendable;
import enkan.web.data.HttpRequest;
import kotowari.inject.ParameterInjector;
import org.jooq.DSLContext;

/**
 * Injects the per-request jOOQ {@link DSLContext} into {@code @Decision} method
 * parameters of resource classes. The DSLContext is placed onto the request as
 * the {@code jooqDslContext} extension by {@code JooqDslContextMiddleware}.
 */
public class DSLContextInjector implements ParameterInjector<DSLContext> {
    @Override
    public String getName() {
        return "dslContext";
    }

    @Override
    public boolean isApplicable(Class<?> type) {
        return DSLContext.class.isAssignableFrom(type);
    }

    @Override
    public DSLContext getInjectObject(HttpRequest request) {
        // HttpRequest itself does not extend Extendable, but middleware (MixinUtils#mixin)
        // returns proxies that implement both. Cast through Object so Java 25's pattern
        // matching does not reject the type pair as statically incompatible.
        Object req = request;
        if (req instanceof Extendable e) {
            return e.getExtension("jooqDslContext");
        }
        return null;
    }
}
