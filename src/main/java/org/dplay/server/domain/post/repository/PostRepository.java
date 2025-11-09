package org.dplay.server.domain.post.repository;

import org.dplay.server.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long>, PostFeedQueryRepository {

    boolean existsByQuestionQuestionIdAndUserUserId(Long questionId, Long userId);

    long countByQuestionQuestionId(Long questionId);
}
