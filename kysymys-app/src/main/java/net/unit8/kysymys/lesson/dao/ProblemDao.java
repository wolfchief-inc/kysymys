package net.unit8.kysymys.lesson.dao;

import net.unit8.kysymys.lesson.data.*;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;

import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

/**
 * jOOQ-backed repository for {@code problems} and {@code problem_lifecycles}.
 * Callers pass the {@link DSLContext} they want to run on; the dao itself
 * never opens transactions.
 */
public class ProblemDao {

    private static final Table<?> PROBLEMS = table("problems");
    private static final Table<?> LIFECYCLES = table("problem_lifecycles");

    // Column references — unqualified (H2 default upper-cases names; unquoted resolves correctly).
    private static final Field<String> ID = field("id", String.class);
    private static final Field<String> NAME = field("name", String.class);
    private static final Field<String> REPOSITORY_URL = field("repository_url", String.class);
    private static final Field<String> BRANCH = field("branch", String.class);
    private static final Field<String> README_PATH = field("readme_path", String.class);
    private static final Field<String> RUNNER = field("runner", String.class);
    private static final Field<String> PROBLEM_LIFECYCLE_ID = field("problem_lifecycle_id", String.class);
    private static final Field<String> PROBLEM_ID = field("problem_id", String.class);
    private static final Field<String> STATUS = field("status", String.class);

    private final DSLContext dsl;

    public ProblemDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * Inserts a Problem and its ProblemLifecycle. The two tables have a
     * mutual FK reference, so we insert the problem with a NULL
     * problem_lifecycle_id, insert the lifecycle, and then update the
     * problem to point back at the lifecycle.
     */
    public void insert(Problem p, ProblemLifecycle lc) {
        dsl.insertInto(PROBLEMS,
                        ID, NAME, REPOSITORY_URL, BRANCH, README_PATH, RUNNER, PROBLEM_LIFECYCLE_ID)
                .values(p.id().value(),
                        p.name().value(),
                        p.repository().url(),
                        branchOf(p.repository()),
                        readmePathOrNull(p.repository()),
                        repositoryTypeKey(p.repository()),
                        null)
                .execute();
        dsl.insertInto(LIFECYCLES, ID, PROBLEM_ID, STATUS)
                .values(lc.id().value(), lc.problemId().value(), lc.status().name())
                .execute();
        dsl.update(PROBLEMS)
                .set(PROBLEM_LIFECYCLE_ID, p.lifecycleId().value())
                .where(ID.eq(p.id().value()))
                .execute();
    }

    public void update(Problem p) {
        dsl.update(PROBLEMS)
                .set(NAME, p.name().value())
                .set(REPOSITORY_URL, p.repository().url())
                .set(BRANCH, branchOf(p.repository()))
                .set(README_PATH, readmePathOrNull(p.repository()))
                .set(RUNNER, repositoryTypeKey(p.repository()))
                .where(ID.eq(p.id().value()))
                .execute();
    }

    public void updateStatus(ProblemLifecycleId lifecycleId, ProblemStatus status) {
        dsl.update(LIFECYCLES)
                .set(STATUS, status.name())
                .where(ID.eq(lifecycleId.value()))
                .execute();
    }

    public Optional<Problem> findById(ProblemId id) {
        Record rec = dsl.select(ID, NAME, REPOSITORY_URL, BRANCH, README_PATH, RUNNER, PROBLEM_LIFECYCLE_ID)
                .from(PROBLEMS)
                .where(ID.eq(id.value()))
                .fetchOne();
        return Optional.ofNullable(rec).map(ProblemDao::mapProblem);
    }

    public Optional<ProblemStatus> findStatus(ProblemLifecycleId lifecycleId) {
        return Optional.ofNullable(
                dsl.select(STATUS).from(LIFECYCLES)
                        .where(ID.eq(lifecycleId.value()))
                        .fetchOne(STATUS))
                .map(ProblemStatus::valueOf);
    }

    public List<Problem> listActive() {
        // Qualify the ambiguous "id" / "status" columns by table for the join.
        Field<String> lifecycleStatus = field("problem_lifecycles.status", String.class);
        Field<String> lifecycleId = field("problem_lifecycles.id", String.class);
        Field<String> problemLifecycleId = field("problems.problem_lifecycle_id", String.class);
        return dsl.select(
                        field("problems.id", String.class),
                        field("problems.name", String.class),
                        field("problems.repository_url", String.class),
                        field("problems.branch", String.class),
                        field("problems.readme_path", String.class),
                        field("problems.runner", String.class),
                        field("problems.problem_lifecycle_id", String.class))
                .from(PROBLEMS)
                .join(LIFECYCLES).on(problemLifecycleId.eq(lifecycleId))
                .where(lifecycleStatus.eq(ProblemStatus.ACTIVE.name()))
                .fetch(r -> mapProblemFromQualified(r));
    }

    // ---- mapping helpers ----

    private static Problem mapProblem(Record r) {
        return mapFromColumns(
                r.get(ID), r.get(NAME), r.get(REPOSITORY_URL), r.get(BRANCH),
                r.get(README_PATH), r.get(RUNNER), r.get(PROBLEM_LIFECYCLE_ID));
    }

    private static Problem mapProblemFromQualified(Record r) {
        return mapFromColumns(
                r.get("problems.id", String.class),
                r.get("problems.name", String.class),
                r.get("problems.repository_url", String.class),
                r.get("problems.branch", String.class),
                r.get("problems.readme_path", String.class),
                r.get("problems.runner", String.class),
                r.get("problems.problem_lifecycle_id", String.class));
    }

    private static Problem mapFromColumns(String id, String name, String url, String branch,
                                          String readmePath, String runner, String lifecycleId) {
        ProblemRepository repo = switch (runner == null ? "generic" : runner) {
            case "github" -> new GitHubProblemRepository(url, branch, readmePath);
            case "bitbucket" -> new BitBucketProblemRepository(url, branch, readmePath);
            case "generic" -> new GenericProblemRepository(url, branch);
            default -> throw new IllegalStateException("unknown repository type: " + runner);
        };
        return new Problem(
                new ProblemId(id),
                new ProblemName(name),
                repo,
                new ProblemLifecycleId(lifecycleId)
        );
    }

    private static String branchOf(ProblemRepository repo) {
        return switch (repo) {
            case GitHubProblemRepository g -> g.branch();
            case BitBucketProblemRepository b -> b.branch();
            case GenericProblemRepository g -> g.branch();
        };
    }

    private static String readmePathOrNull(ProblemRepository repo) {
        return switch (repo) {
            case GitHubProblemRepository g -> g.readmePath();
            case BitBucketProblemRepository b -> b.readmePath();
            case GenericProblemRepository g -> null;
        };
    }

    /** The {@code runner} column doubles as the discriminator for {@link ProblemRepository}. */
    private static String repositoryTypeKey(ProblemRepository repo) {
        return switch (repo) {
            case GitHubProblemRepository g -> "github";
            case BitBucketProblemRepository b -> "bitbucket";
            case GenericProblemRepository g -> "generic";
        };
    }
}
