package net.unit8.kysymys;

import enkan.collection.OptionMap;
import enkan.component.ApplicationComponent;
import enkan.component.flyway.FlywayMigration;
import enkan.component.hikaricp.HikariCPComponent;
import enkan.component.jackson.JacksonBeansConverter;
import enkan.component.jooq.JooqProvider;
import enkan.component.undertow.UndertowComponent;
import enkan.config.EnkanSystemFactory;
import enkan.system.EnkanSystem;
import net.unit8.kysymys.system.KysymysEventBus;
import org.jooq.SQLDialect;

import static enkan.component.ComponentRelationship.component;
import static enkan.util.BeanBuilder.builder;

/**
 * Component wiring for development. Uses an in-memory H2 database and a fixed
 * JWT HMAC secret so that the server can be started by {@code mvn exec:java}
 * without any environment variables.
 *
 * <p>Component dependency graph:
 * <pre>
 *   datasource ← flyway   (runs Flyway migrations on startup)
 *   datasource ← jooq     (jOOQ DSLContext backed by HikariCP)
 *   jooq, beans, flyway ← app
 *   app ← http            (Undertow on port 3000)
 * </pre>
 */
public class KysymysDevSystemFactory implements EnkanSystemFactory {

    /** HMAC secret used by the {@code BouncrBackend} configured in {@link KysymysApplicationFactory}. */
    public static final String DEV_JWT_SECRET = "kysymys-dev-jwt-secret-not-for-production";

    @Override
    public EnkanSystem create() {
        // Surface the dev secret so KysymysApplicationFactory can pick it up via env / system property.
        System.setProperty("KYSYMYS_JWT_SECRET", DEV_JWT_SECRET);

        return EnkanSystem.of(
                "jooq", builder(new JooqProvider())
                        .set(JooqProvider::setDialect, SQLDialect.H2)
                        .build(),
                "datasource", builder(new HikariCPComponent(OptionMap.of(
                                "uri", "jdbc:h2:mem:kysymys;DB_CLOSE_DELAY=-1"
                        )))
                        .build(),
                "flyway", new FlywayMigration(),
                "beans", new JacksonBeansConverter(),
                "eventBus", new KysymysEventBus(),
                "app", new ApplicationComponent<>("net.unit8.kysymys.KysymysApplicationFactory"),
                "http", builder(new UndertowComponent())
                        .set(UndertowComponent::setPort, 3000)
                        .build()
        ).relationships(
                component("flyway").using("datasource"),
                component("jooq").using("datasource"),
                component("app").using("jooq", "beans", "flyway", "eventBus"),
                component("http").using("app")
        );
    }
}
