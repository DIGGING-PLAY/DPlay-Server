package org.dplay.server.domain.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.dplay.server.domain.auth.service.AuthService;
import org.dplay.server.domain.s3.S3Service;
import org.dplay.server.domain.user.Platform;
import org.dplay.server.domain.user.entity.User;
import org.dplay.server.domain.user.repository.UserRepository;
import org.dplay.server.domain.user.service.UserService;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;
import org.dplay.server.global.util.NicknameValidator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final S3Service s3Service;
    private final NicknameValidator nicknameValidator;

    @Override
    public void updateProfileImage(String accessToken, MultipartFile profileImg) throws IOException {
        User user = authService.getUserFromToken(accessToken);

        String profileImgUrl = (profileImg == null) ? null
                : profileImg.isEmpty() ? ""
                : s3Service.uploadImage(profileImg);

        if (profileImgUrl == null) {
            return;
        } else if (profileImgUrl.isEmpty()) {
            user.updateProfileImg(null);
        } else {
            user.updateProfileImg(profileImgUrl);
        }
    }

    @Override
    public void updateNickname(String accessToken, String nickname) {
        User user = authService.getUserFromToken(accessToken);

        if (Objects.equals(user.getNickname(), nickname)) {
            return;
        } else if (existsByNickname(nickname)) {
            throw new DPlayException(ResponseError.RESOURCE_ALREADY_EXISTS);
        } else {
            nicknameValidator.validate(nickname);
            user.updateNickname(nickname);
        }
    }

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

    public User save(final User user){
        return userRepository.save(user);
    }
}
