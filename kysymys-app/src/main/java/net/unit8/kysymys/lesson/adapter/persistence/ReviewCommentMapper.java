package net.unit8.kysymys.lesson.adapter.persistence;

import net.unit8.kysymys.lesson.domain.Comment;
import net.unit8.kysymys.lesson.domain.CommentId;
import net.unit8.kysymys.lesson.domain.Description;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.stereotype.Component;

@Component
class ReviewCommentMapper {
    private final AnswerMapper answerMapper;

    ReviewCommentMapper(AnswerMapper answerMapper) {
        this.answerMapper = answerMapper;
    }

    ReviewCommentJpaEntity domainToEntity(Comment comment) {
        ReviewCommentJpaEntity entity = new ReviewCommentJpaEntity();
        entity.setId(comment.getId().getValue());
        entity.setAnswer(answerMapper.domainToEntity(comment.getAnswer()));
        entity.setCommenterId(comment.getCommenterId().getValue());
        entity.setDescription(comment.getDescription().getValue());
        entity.setPostedAt(comment.getPostedAt());
        return entity;
    }

    Comment entityToDomain(ReviewCommentJpaEntity entity) {
        return new Comment(
                CommentId.of(entity.getId()),
                answerMapper.entityToDomain(entity.getAnswer()),
                UserId.of(entity.getCommenterId()),
                Description.of(entity.getDescription()),
                entity.getPostedAt()
        );
    }
}
