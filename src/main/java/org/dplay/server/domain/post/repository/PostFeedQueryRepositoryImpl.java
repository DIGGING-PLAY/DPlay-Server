package org.dplay.server.domain.post.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.dplay.server.domain.post.entity.Post;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Repository
public class PostFeedQueryRepositoryImpl implements PostFeedQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Post> findFeedPosts(
            Long questionId,
            Long cursorLikeCount,
            Long cursorPostId,
            int limit,
            List<Long> excludePostIds
    ) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM Post p ");
        jpql.append("JOIN FETCH p.user u ");
        jpql.append("JOIN FETCH p.track t ");
        jpql.append("WHERE p.question.questionId = :questionId ");

        if (!CollectionUtils.isEmpty(excludePostIds)) {
            jpql.append("AND p.postId NOT IN :excludeIds ");
        }

        if (cursorLikeCount != null && cursorPostId != null) {
            jpql.append("AND (p.likeCount < :cursorLikeCount ")
                    .append("OR (p.likeCount = :cursorLikeCount AND p.postId > :cursorPostId)) ");
        }

        jpql.append("ORDER BY p.likeCount DESC, p.postId ASC");

        TypedQuery<Post> query = entityManager.createQuery(jpql.toString(), Post.class)
                .setParameter("questionId", questionId)
                .setMaxResults(limit);

        if (!CollectionUtils.isEmpty(excludePostIds)) {
            query.setParameter("excludeIds", excludePostIds);
        }

        if (cursorLikeCount != null && cursorPostId != null) {
            query.setParameter("cursorLikeCount", cursorLikeCount);
            query.setParameter("cursorPostId", cursorPostId);
        }

        return query.getResultList();
    }

    @Override
    public List<Post> findLatestPosts(
            Long questionId,
            int limit,
            List<Long> excludePostIds
    ) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM Post p ");
        jpql.append("JOIN FETCH p.user u ");
        jpql.append("JOIN FETCH p.track t ");
        jpql.append("WHERE p.question.questionId = :questionId ");

        if (!CollectionUtils.isEmpty(excludePostIds)) {
            jpql.append("AND p.postId NOT IN :excludeIds ");
        }

        jpql.append("ORDER BY p.createdAt DESC, p.postId DESC");

        TypedQuery<Post> query = entityManager.createQuery(jpql.toString(), Post.class)
                .setParameter("questionId", questionId)
                .setMaxResults(limit);

        if (!CollectionUtils.isEmpty(excludePostIds)) {
            query.setParameter("excludeIds", excludePostIds);
        }

        return query.getResultList();
    }

    @Override
    public List<Post> findAllFeedPosts(Long questionId, List<Long> excludePostIds) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM Post p ");
        jpql.append("JOIN FETCH p.user u ");
        jpql.append("JOIN FETCH p.track t ");
        jpql.append("WHERE p.question.questionId = :questionId ");

        if (!CollectionUtils.isEmpty(excludePostIds)) {
            jpql.append("AND p.postId NOT IN :excludeIds ");
        }

        jpql.append("ORDER BY p.likeCount DESC, p.postId ASC");

        TypedQuery<Post> query = entityManager.createQuery(jpql.toString(), Post.class)
                .setParameter("questionId", questionId);

        if (!CollectionUtils.isEmpty(excludePostIds)) {
            query.setParameter("excludeIds", excludePostIds);
        }

        return query.getResultList();
    }

    @Override
    public List<Post> findPostsByUserDesc(
            Long userId,
            Long cursorPostId,
            int limit
    ) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM Post p ");
        jpql.append("JOIN FETCH p.user u ");
        jpql.append("JOIN FETCH p.track t ");
        jpql.append("WHERE p.user.userId = :userId ");

        if (cursorPostId != null) {
            jpql.append("AND p.postId < :cursorPostId ");
        }

        jpql.append("ORDER BY p.postId DESC");

        TypedQuery<Post> query = entityManager.createQuery(jpql.toString(), Post.class)
                .setParameter("userId", userId)
                .setMaxResults(limit);

        if (cursorPostId != null) {
            query.setParameter("cursorPostId", cursorPostId);
        }

        return query.getResultList();
    }
}
