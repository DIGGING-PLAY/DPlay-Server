package org.dplay.server.domain.post.repository;

import org.dplay.server.domain.post.entity.Post;
import org.dplay.server.domain.post.entity.PostSave;
import org.dplay.server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    @Query("SELECT ps.post.postId FROM PostSave ps WHERE ps.post IN :posts AND ps.user = :user")
    List<Long> findPostIdsByUserAndPosts(@Param("user") User user, @Param("posts") List<Post> posts);

    /**
     * 특정 유저가 스크랩한 PostSave의 개수를 조회합니다.
     *
     * @param userId 유저 ID
     * @return 스크랩한 글 개수
     */
    long countByUserUserId(Long userId);

    void deleteAllByUser(User user);
}

