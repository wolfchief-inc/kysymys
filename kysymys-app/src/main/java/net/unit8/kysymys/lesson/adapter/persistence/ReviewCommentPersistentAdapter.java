package net.unit8.kysymys.lesson.adapter.persistence;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import net.unit8.kysymys.lesson.application.SaveCommentPort;
import net.unit8.kysymys.lesson.application.impl.ListCommentPort;
import net.unit8.kysymys.lesson.domain.AnswerId;
import net.unit8.kysymys.lesson.domain.Comment;
import net.unit8.kysymys.steleotype.PersistenceAdapter;
import org.springframework.data.domain.*;
import org.springframework.util.comparator.Comparators;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@PersistenceAdapter
class ReviewCommentPersistentAdapter implements SaveCommentPort, ListCommentPort {
    private final ReviewCommentRepository reviewCommentRepository;
    private final ReviewCommentMapper reviewCommentMapper;

    ReviewCommentPersistentAdapter(ReviewCommentRepository reviewCommentRepository, ReviewCommentMapper reviewCommentMapper) {
        this.reviewCommentRepository = reviewCommentRepository;
        this.reviewCommentMapper = reviewCommentMapper;
    }

    @Override
    public List<Comment> listRecentComments(AnswerId answerId, String cursor, int size) {
        Pageable page = PageRequest.of(0, size, Sort.by("id").descending());
        return reviewCommentRepository.findByCursor(answerId.getValue(), cursor, page)
                .stream()
                .sorted(Comparator.comparing(ReviewCommentJpaEntity::getId))
                .map(reviewCommentMapper::entityToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void save(Comment comment) {
        reviewCommentRepository.save(reviewCommentMapper.domainToEntity(comment));
    }
}
