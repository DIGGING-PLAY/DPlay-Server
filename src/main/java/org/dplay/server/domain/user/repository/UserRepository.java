package org.dplay.server.domain.user.repository;

import org.dplay.server.domain.user.Platform;
import org.dplay.server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByPlatformIdAndPlatform(String serialId, Platform platform);

    Optional<User> findByPlatformIdAndPlatform(String platformId, Platform Platform);
}
