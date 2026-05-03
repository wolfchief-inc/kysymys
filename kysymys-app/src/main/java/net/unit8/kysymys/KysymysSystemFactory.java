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
import org.jooq.SQLDialect;

import static enkan.component.ComponentRelationship.component;
import static enkan.util.BeanBuilder.builder;

/**
 * Component wiring for production. Reads database settings and the JWT HMAC
 * secret from environment variables. All four are required:
 * <ul>
 *   <li>{@code KYSYMYS_DB_URL} — JDBC URL (e.g. {@code jdbc:postgresql://host:5432/kysymys})</li>
 *   <li>{@code KYSYMYS_DB_USER} — database user</li>
 *   <li>{@code KYSYMYS_DB_PASSWORD} — database password</li>
 *   <li>{@code KYSYMYS_JWT_SECRET} — HMAC shared secret for {@code BouncrBackend}</li>
 * </ul>
 */
public class KysymysSystemFactory implements EnkanSystemFactory {

    @Override
    public EnkanSystem create() {
        String dbUrl = requireEnv("KYSYMYS_DB_URL");
        String dbUser = requireEnv("KYSYMYS_DB_USER");
        String dbPassword = requireEnv("KYSYMYS_DB_PASSWORD");
        requireEnv("KYSYMYS_JWT_SECRET");

        return EnkanSystem.of(
                "jooq", builder(new JooqProvider())
                        .set(JooqProvider::setDialect, SQLDialect.POSTGRES)
                        .build(),
                "datasource", builder(new HikariCPComponent(OptionMap.of(
                                "uri", dbUrl,
                                "username", dbUser,
                                "password", dbPassword
                        )))
                        .build(),
                "flyway", new FlywayMigration(),
                "beans", new JacksonBeansConverter(),
                "app", new ApplicationComponent<>("net.unit8.kysymys.KysymysApplicationFactory"),
                "http", builder(new UndertowComponent())
                        .set(UndertowComponent::setPort, 3000)
                        .build()
        ).relationships(
                component("flyway").using("datasource"),
                component("jooq").using("datasource"),
                component("app").using("jooq", "beans", "flyway"),
                component("http").using("app")
        );
    }

    private static String requireEnv(String name) {
        String v = System.getenv(name);
        if (v == null || v.isBlank()) {
            throw new IllegalStateException("Required environment variable not set: " + name);
        }
        return v;
    }
}
