package org.dplay.server.domain.post.repository;

import org.dplay.server.domain.post.entity.Post;
import org.dplay.server.domain.post.entity.PostLike;
import org.dplay.server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    /**
     * 특정 Post와 User로 PostLike가 존재하는지 확인합니다.
     *
     * @param post Post 엔티티
     * @param user User 엔티티
     * @return 존재 여부
     */
    boolean existsByPostAndUser(Post post, User user);

    /**
     * 특정 Post와 User로 PostLike를 조회합니다.
     *
     * @param post Post 엔티티
     * @param user User 엔티티
     * @return PostLike (없으면 Optional.empty())
     */
    Optional<PostLike> findByPostAndUser(Post post, User user);
}
