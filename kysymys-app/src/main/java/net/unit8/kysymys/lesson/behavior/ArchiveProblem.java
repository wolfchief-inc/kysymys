package net.unit8.kysymys.lesson.behavior;

import net.unit8.kysymys.lesson.dao.ProblemDao;
import net.unit8.kysymys.lesson.dao.ProblemEventDao;
import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;

import java.time.LocalDateTime;
import java.util.Optional;

public class ArchiveProblem {
    private final DSLContext dsl;

    public ArchiveProblem(DSLContext dsl) {
        this.dsl = dsl;
    }

    public boolean apply(Input in) {
        Optional<Problem> existing = new ProblemDao(dsl).findById(in.problemId());
        if (existing.isEmpty()) return false;

        ProblemArchivedEvent event = new ProblemArchivedEvent(
                ProblemEventId.newId(),
                existing.get().lifecycleId(),
                in.now(),
                in.archiverId());

        dsl.transaction(cfg -> {
            new ProblemDao(cfg.dsl()).updateStatus(existing.get().lifecycleId(), ProblemStatus.ARCHIVED);
            new ProblemEventDao(cfg.dsl()).insert(event);
        });
        return true;
    }

    public record Input(
            ProblemId problemId,
            UserId archiverId,
            LocalDateTime now
    ) {}
}
