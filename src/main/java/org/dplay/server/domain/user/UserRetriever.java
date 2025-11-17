package org.dplay.server.domain.user;

import lombok.RequiredArgsConstructor;
import org.dplay.server.domain.user.entity.User;
import org.dplay.server.domain.user.repository.UserRepository;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRetriever {

    private final UserRepository userRepository;

    public boolean existsByProviderIdAndProvider(final String providerId, final Platform platform) {
        return userRepository.existsByPlatformIdAndPlatform(providerId, platform);
    }

    public boolean existsByNickname(final String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public User findByProviderIdAndProvider(final String providerId, Platform platform) {
        return userRepository.findByPlatformIdAndPlatform(providerId, platform)
                .orElseThrow(() -> new DPlayException(ResponseError.USER_NOT_FOUND));
    }
}
