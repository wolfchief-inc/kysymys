package net.unit8.kysymys.lesson.behavior;

import net.unit8.kysymys.lesson.dao.ProblemDao;
import net.unit8.kysymys.lesson.dao.ProblemEventDao;
import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;

import java.time.LocalDateTime;
import java.util.Optional;

public class UpdateProblem {
    private final DSLContext dsl;

    public UpdateProblem(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Optional<Problem> apply(Input in) {
        ProblemDao problems = new ProblemDao(dsl);
        Optional<Problem> existing = problems.findById(in.problemId());
        if (existing.isEmpty()) return Optional.empty();

        Problem updated = new Problem(
                existing.get().id(),
                in.name(),
                in.repository(),
                existing.get().lifecycleId());
        ProblemUpdatedEvent event = new ProblemUpdatedEvent(
                ProblemEventId.newId(),
                existing.get().lifecycleId(),
                in.now(),
                in.updaterId());

        dsl.transaction(cfg -> {
            new ProblemDao(cfg.dsl()).update(updated);
            new ProblemEventDao(cfg.dsl()).insert(event);
        });
        return Optional.of(updated);
    }

    public record Input(
            ProblemId problemId,
            ProblemName name,
            ProblemRepository repository,
            UserId updaterId,
            LocalDateTime now
    ) {}
}
