package org.dplay.server.domain.user.service;

import org.dplay.server.domain.user.Platform;
import org.dplay.server.domain.user.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserService {
    void updateProfileImage(String accessToken, MultipartFile profileImg) throws IOException;

    void updateNickname(String accessToken, String nickname);

    boolean existsByProviderIdAndProvider(final String providerId, final Platform platform);

    boolean existsByNickname(final String nickname);

    User findByProviderIdAndProvider(final String providerId, Platform platform);
}
