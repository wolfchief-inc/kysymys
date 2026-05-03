package net.unit8.kysymys;

import enkan.Application;
import enkan.config.ApplicationFactory;
import enkan.middleware.AuthenticationMiddleware;
import enkan.middleware.jooq.JooqDslContextMiddleware;
import enkan.security.AuthBackend;
import enkan.security.bouncr.BouncrBackend;

import java.util.Map;
import enkan.system.inject.ComponentInjector;
import enkan.web.application.WebApplication;
import enkan.web.data.HttpRequest;
import enkan.web.data.HttpResponse;
import enkan.web.middleware.ContentNegotiationMiddleware;
import enkan.web.middleware.NestedParamsMiddleware;
import enkan.web.middleware.ParamsMiddleware;
import kotowari.inject.ParameterInjector;
import kotowari.inject.parameter.HttpRequestInjector;
import kotowari.inject.parameter.ParametersInjector;
import kotowari.inject.parameter.PrincipalInjector;
import kotowari.middleware.RoutingMiddleware;
import kotowari.middleware.SerDesMiddleware;
import kotowari.restful.middleware.ResourceInvokerMiddleware;
import kotowari.routing.Routes;
import net.unit8.kysymys.health.HealthResource;
import net.unit8.kysymys.health.MeResource;
import net.unit8.kysymys.inject.DSLContextInjector;
import net.unit8.kysymys.inject.UserIdInjector;

import java.util.List;
import java.util.Set;

import static enkan.util.BeanBuilder.builder;

/**
 * Builds the middleware stack for Kysymys. Shared by both
 * {@link KysymysDevSystemFactory} (development) and
 * {@link KysymysSystemFactory} (production); the only difference between the
 * two is component wiring (datasource, JWT secret), not the middleware order.
 *
 * <p>Authentication: a single {@link BouncrBackend} configured with an HMAC
 * secret read from the {@code KYSYMYS_JWT_SECRET} environment variable
 * (or system property, used by {@link KysymysDevSystemFactory} to inject a
 * fixed dev secret).
 */
public class KysymysApplicationFactory implements ApplicationFactory<HttpRequest, HttpResponse> {

    @Override
    public Application<HttpRequest, HttpResponse> create(ComponentInjector injector) {
        List<ParameterInjector<?>> parameterInjectors = List.of(
                new HttpRequestInjector(),
                new ParametersInjector(),
                new PrincipalInjector(),
                new DSLContextInjector(),
                new UserIdInjector()
        );

        ResourceInvokerMiddleware<HttpResponse> resourceInvoker =
                builder(new ResourceInvokerMiddleware<HttpResponse>(injector))
                        .set(ResourceInvokerMiddleware::setParameterInjectors, parameterInjectors)
                        .set(ResourceInvokerMiddleware::setOutputErrorReason, true)
                        .build();

        Routes routes = Routes.define(r -> {
            r.get("/health").to(HealthResource.class);
            r.get("/me").to(MeResource.class);
        }).compile();

        BouncrBackend bouncrBackend = new BouncrBackend();
        bouncrBackend.setKey(jwtSecret());
        List<AuthBackend<HttpRequest, Map<String, Object>>> backends = List.of(bouncrBackend);

        WebApplication app = new WebApplication();
        app.use(new ParamsMiddleware());
        app.use(new NestedParamsMiddleware());
        app.use(builder(new ContentNegotiationMiddleware())
                .set(ContentNegotiationMiddleware::setAllowedTypes, Set.of("application/json"))
                .build());
        app.use(new RoutingMiddleware(routes));
        app.use(new JooqDslContextMiddleware<>());
        app.use(new SerDesMiddleware<>());
        app.use(new AuthenticationMiddleware<>(backends));
        app.use(resourceInvoker);
        return app;
    }

    private static String jwtSecret() {
        String v = System.getenv("KYSYMYS_JWT_SECRET");
        if (v == null || v.isBlank()) {
            v = System.getProperty("KYSYMYS_JWT_SECRET");
        }
        if (v == null || v.isBlank()) {
            throw new IllegalStateException(
                    "KYSYMYS_JWT_SECRET is not set. KysymysDevSystemFactory injects a dev " +
                    "default; for production set the environment variable explicitly.");
        }
        return v;
    }
}
