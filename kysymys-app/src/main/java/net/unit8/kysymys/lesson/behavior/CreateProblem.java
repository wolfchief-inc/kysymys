package net.unit8.kysymys.lesson.behavior;

import net.unit8.kysymys.lesson.dao.ProblemDao;
import net.unit8.kysymys.lesson.dao.ProblemEventDao;
import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;

import java.time.LocalDateTime;

/**
 * Atomically inserts a new Problem along with its lifecycle and a
 * {@link ProblemCreatedEvent}. Returns the persisted Problem.
 */
public class CreateProblem {
    private final DSLContext dsl;

    public CreateProblem(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Problem apply(Input input) {
        ProblemId pid = ProblemId.newId();
        ProblemLifecycleId lid = ProblemLifecycleId.newId();
        Problem problem = new Problem(pid, input.name(), input.repository(), lid);
        ProblemLifecycle lifecycle = new ProblemLifecycle(lid, pid, ProblemStatus.ACTIVE);
        ProblemCreatedEvent event = new ProblemCreatedEvent(
                ProblemEventId.newId(),
                lid,
                input.now(),
                input.creatorId());

        dsl.transaction(cfg -> {
            new ProblemDao(cfg.dsl()).insert(problem, lifecycle);
            new ProblemEventDao(cfg.dsl()).insert(event);
        });
        return problem;
    }

    public record Input(
            ProblemName name,
            ProblemRepository repository,
            UserId creatorId,
            LocalDateTime now
    ) {}
}
