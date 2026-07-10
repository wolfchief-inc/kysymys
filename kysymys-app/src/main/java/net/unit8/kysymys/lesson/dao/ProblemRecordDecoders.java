package net.unit8.kysymys.lesson.dao;

import net.unit8.kysymys.lesson.data.BitBucketProblemRepository;
import net.unit8.kysymys.lesson.data.GenericProblemRepository;
import net.unit8.kysymys.lesson.data.GitHubProblemRepository;
import net.unit8.kysymys.lesson.data.Problem;
import net.unit8.kysymys.lesson.data.ProblemId;
import net.unit8.kysymys.lesson.data.ProblemLifecycleId;
import net.unit8.kysymys.lesson.data.ProblemName;
import net.unit8.kysymys.lesson.data.ProblemRepository;
import net.unit8.raoh.decode.Decoder;
import org.jooq.Record;

import java.util.Map;

import static net.unit8.raoh.decode.ObjectDecoders.string;
import static net.unit8.raoh.jooq.JooqRecordDecoders.combine;
import static net.unit8.raoh.jooq.JooqRecordDecoders.discriminate;
import static net.unit8.raoh.jooq.JooqRecordDecoders.field;

/**
 * raoh-jooq decoders mapping {@code problems} rows (optionally joined with
 * {@code problem_lifecycles}) into the {@link Problem} aggregate.
 *
 * <p>Mirror image of the request-side {@code LessonJsonDecoders}: the {@code runner}
 * column is the discriminator for {@link ProblemRepository}, exactly as {@code "type"}
 * is on the JSON side, so the read and write boundaries share one shape.
 */
public final class ProblemRecordDecoders {
    private ProblemRecordDecoders() {}

    private static final Decoder<Record, ProblemRepository> REPOSITORY = discriminate("runner", Map.of(
            "github", combine(
                    field("repository_url", string()),
                    field("branch", string()),
                    field("readme_path", string())
            ).map((url, branch, readme) -> (ProblemRepository) new GitHubProblemRepository(url, branch, readme)),
            "bitbucket", combine(
                    field("repository_url", string()),
                    field("branch", string()),
                    field("readme_path", string())
            ).map((url, branch, readme) -> (ProblemRepository) new BitBucketProblemRepository(url, branch, readme)),
            "generic", combine(
                    field("repository_url", string()),
                    field("branch", string())
            ).map((url, branch) -> (ProblemRepository) new GenericProblemRepository(url, branch))
    ));

    public static final Decoder<Record, Problem> PROBLEM = combine(
            field("id", string()).map(ProblemId::new),
            field("name", string()).map(ProblemName::new),
            REPOSITORY,
            field("problem_lifecycle_id", string()).map(ProblemLifecycleId::new)
    ).map(Problem::new);
}
