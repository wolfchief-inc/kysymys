package net.unit8.kysymys.lesson.adapter.persistence;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import net.unit8.kysymys.lesson.application.SaveProblemEventPort;
import net.unit8.kysymys.lesson.domain.CreatedProblemEvent;
import net.unit8.kysymys.lesson.domain.ProblemStatus;
import net.unit8.kysymys.stereotype.PersistenceAdapter;

@PersistenceAdapter
public class ProblemLifecyclePersistenceAdapter implements SaveProblemEventPort {
    private final ProblemLifecycleRepository problemLifecycleRepository;
    private final ProblemEventRepository problemEventRepository;
    private final ProblemRepository problemRepository;

    public ProblemLifecyclePersistenceAdapter(ProblemLifecycleRepository problemLifecycleRepository, ProblemEventRepository problemEventRepository, ProblemRepository problemRepository) {
        this.problemLifecycleRepository = problemLifecycleRepository;
        this.problemEventRepository = problemEventRepository;
        this.problemRepository = problemRepository;
    }

    @Override
    public void save(CreatedProblemEvent event) {
        ProblemLifecycleJpaEntity problemLifecycle = problemLifecycleRepository.findByProblemId(event.getProblemId())
                .orElseGet(() -> {
                    ProblemLifecycleJpaEntity pje = new ProblemLifecycleJpaEntity();
                    pje.setId(NanoIdUtils.randomNanoId());
                    pje.setProblem(problemRepository.getById(event.getProblemId()));
                    return pje;
                });
        problemLifecycle.setStatus(ProblemStatus.ACTIVE);
        problemLifecycleRepository.save(problemLifecycle);

        ProblemCreatedJpaEntity problemCreatedEntity = new ProblemCreatedJpaEntity();
        problemCreatedEntity.setId(NanoIdUtils.randomNanoId());
        problemCreatedEntity.setCreatorId(event.getCreatorId());
        problemCreatedEntity.setProblemLifecycle(problemLifecycle);
        problemCreatedEntity.setOccurredAt(event.getOccurredAt());
        problemEventRepository.save(problemCreatedEntity);
    }
}
