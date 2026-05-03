package net.unit8.kysymys.lesson.dao;

import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.sql.DataSource;
import java.util.UUID;

/**
 * Test-only helper. Creates a private in-memory H2 database, runs Flyway,
 * and exposes a jOOQ {@link DSLContext}. Each dao test class instantiates
 * one of these in {@code @BeforeAll}.
 */
public final class DaoTestSupport implements AutoCloseable {
    private final DataSource dataSource;
    private final DSLContext dsl;

    public DaoTestSupport() {
        JdbcDataSource ds = new JdbcDataSource();
        // Unique URL per instance keeps parallel-running tests isolated.
        ds.setUrl("jdbc:h2:mem:test-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");
        Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration")
                .load()
                .migrate();
        this.dataSource = ds;
        this.dsl = DSL.using(ds, SQLDialect.H2);
    }

    public DSLContext dsl() {
        return dsl;
    }

    public DataSource dataSource() {
        return dataSource;
    }

    @Override
    public void close() {
        // H2 in-mem with DB_CLOSE_DELAY=-1 lives until JVM exit; nothing to do.
    }
}
