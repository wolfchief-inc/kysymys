package net.unit8.kysymys.lesson.adapter.persistence;

import net.unit8.kysymys.lesson.application.SaveCommentPort;
import net.unit8.kysymys.lesson.domain.Comment;
import net.unit8.kysymys.steleotype.PersistenceAdapter;

@PersistenceAdapter
class ReviewCommentPersistentAdapter implements SaveCommentPort {
    private final ReviewCommentRepository reviewCommentRepository;
    private final ReviewCommentMapper reviewCommentMapper;

    ReviewCommentPersistentAdapter(ReviewCommentRepository reviewCommentRepository, ReviewCommentMapper reviewCommentMapper) {
        this.reviewCommentRepository = reviewCommentRepository;
        this.reviewCommentMapper = reviewCommentMapper;
    }

    @Override
    public void save(Comment comment) {
        reviewCommentRepository.save(reviewCommentMapper.domainToEntity(comment));
    }
}
