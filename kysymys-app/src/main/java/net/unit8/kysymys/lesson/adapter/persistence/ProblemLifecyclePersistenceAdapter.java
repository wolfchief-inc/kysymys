package net.unit8.kysymys.lesson.adapter.persistence;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import net.unit8.kysymys.lesson.domain.CreatedProblemEvent;
import net.unit8.kysymys.stereotype.PersistenceAdapter;

@PersistenceAdapter
public class ProblemLifecyclePersistenceAdapter {
    private final ProblemLifecycleRepository problemLifecycleRepository;
    private final ProblemEventRepository problemEventRepository;

    public ProblemLifecyclePersistenceAdapter(ProblemLifecycleRepository problemLifecycleRepository, ProblemEventRepository problemEventRepository) {
        this.problemLifecycleRepository = problemLifecycleRepository;
        this.problemEventRepository = problemEventRepository;
    }

    public void save(CreatedProblemEvent event) {
        ProblemLifecycleJpaEntity problemLifecycle = problemLifecycleRepository.findByProblemId(event.getProblemId());
        ProblemCreatedJpaEntity problemCreatedEntity = new ProblemCreatedJpaEntity();
        problemCreatedEntity.setId(NanoIdUtils.randomNanoId());
        problemCreatedEntity.setCreatorId(event.getCreatorId());
        problemEventRepository.save(problemCreatedEntity);
    }
}
