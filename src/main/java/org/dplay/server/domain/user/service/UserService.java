package org.dplay.server.domain.user.service;

import org.dplay.server.domain.user.Platform;
import org.dplay.server.domain.user.dto.UserProfileDto;
import org.dplay.server.domain.user.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserService {
    void updateProfileImage(Long userId, MultipartFile profileImg) throws IOException;

    void updateNickname(Long userId, String nickname);

    void updateNotification(Long userId, Boolean pushOn);

    UserProfileDto getUserProfile(Long userId, Long authorizationUserId);

    boolean existsByProviderIdAndProvider(String providerId, Platform platform);

    boolean existsByNickname(String nickname);

    User findUserByProviderIdAndProvider(String providerId, Platform platform);

    User findByRefreshToken(final String refreshToken);

    User getUserById(Long userId);

    User makeUser(String platformId, Platform platform, String nickname, MultipartFile profileImg) throws IOException;
}
