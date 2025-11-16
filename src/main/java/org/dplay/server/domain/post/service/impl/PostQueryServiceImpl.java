package org.dplay.server.domain.post.service.impl;

import lombok.RequiredArgsConstructor;
import org.dplay.server.domain.post.entity.Post;
import org.dplay.server.domain.post.repository.PostRepository;
import org.dplay.server.domain.post.service.PostQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryServiceImpl implements PostQueryService {

    private final PostRepository postRepository;

    @Override
    public boolean existsByQuestionAndUser(Long questionId, Long userId) {
        return postRepository.existsByQuestionQuestionIdAndUserUserId(questionId, userId);
    }

    @Override
    public long countByQuestion(Long questionId) {
        return postRepository.countByQuestionQuestionId(questionId);
    }

    @Override
    public List<Post> findFeedPosts(
            Long questionId,
            Long cursorLikeCount,
            Long cursorPostId,
            int limit,
            List<Long> excludePostIds
    ) {
        return postRepository.findFeedPosts(questionId, cursorLikeCount, cursorPostId, limit, excludePostIds);
    }

    @Override
    public List<Post> findLatestPosts(
            Long questionId,
            int limit,
            List<Long> excludePostIds
    ) {
        return postRepository.findLatestPosts(questionId, limit, excludePostIds);
    }

    @Override
    public List<Post> findAllFeedPosts(Long questionId, List<Long> excludePostIds) {
        return postRepository.findAllFeedPosts(questionId, excludePostIds);
    }
}
