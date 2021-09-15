package net.unit8.kysymys.lesson.adapter.persistence;

import net.unit8.kysymys.lesson.application.DeleteProblemPort;
import net.unit8.kysymys.lesson.application.GetProblemsPort;
import net.unit8.kysymys.lesson.application.LoadProblemPort;
import net.unit8.kysymys.lesson.application.SaveProblemPort;
import net.unit8.kysymys.lesson.domain.Problem;
import net.unit8.kysymys.lesson.domain.ProblemId;
import net.unit8.kysymys.stereotype.PersistenceAdapter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

@PersistenceAdapter
public class ProblemPersistenceAdapter implements LoadProblemPort, SaveProblemPort, GetProblemsPort, DeleteProblemPort {
    private final ProblemRepository problemRepository;
    private final ProblemMapper problemMapper;

    public ProblemPersistenceAdapter(ProblemRepository problemRepository, ProblemMapper problemMapper) {
        this.problemRepository = problemRepository;
        this.problemMapper = problemMapper;
    }

    @Override
    public Optional<Problem> load(ProblemId problemId) {
        return problemRepository.findById(problemId.getValue())
                .map(problemMapper::entityToDomain);
    }

    @Override
    public void save(Problem problem) {
        Optional.ofNullable(problem)
                .map(problemMapper::domainToEntity)
                .ifPresent(problemRepository::save);
    }

    @Override
    public Page<Problem> list(int page) {
        if (page > 0) page = page - 1;
        return problemRepository.findAll(PageRequest.of(page, 10))
                .map(problemMapper::entityToDomain);
    }

    @Override
    public void delete(ProblemId problemId) {
        problemRepository.deleteById(problemId.getValue());
    }
}
