package org.dplay.server.domain.post.repository;

import org.dplay.server.domain.post.entity.Post;
import org.dplay.server.domain.post.entity.PostSave;
import org.dplay.server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostSaveRepository extends JpaRepository<PostSave, Long> {
    /**
     * 특정 Post와 User로 PostSave가 존재하는지 확인합니다.
     *
     * @param post Post 엔티티
     * @param user User 엔티티
     * @return 존재 여부
     */
    boolean existsByPostAndUser(Post post, User user);

    /**
     * 특정 Post와 User로 PostSave를 조회합니다.
     *
     * @param post Post 엔티티
     * @param user User 엔티티
     * @return PostSave (없으면 Optional.empty())
     */
    Optional<PostSave> findByPostAndUser(Post post, User user);
}

