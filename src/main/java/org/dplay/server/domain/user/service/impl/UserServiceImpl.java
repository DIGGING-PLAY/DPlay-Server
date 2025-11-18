package org.dplay.server.domain.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.dplay.server.domain.s3.S3Service;
import org.dplay.server.domain.user.Platform;
import org.dplay.server.domain.user.entity.User;
import org.dplay.server.domain.user.repository.UserRepository;
import org.dplay.server.domain.user.service.UserService;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;
import org.dplay.server.global.util.NicknameValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final NicknameValidator nicknameValidator;

    @Override
    @Transactional
    public void updateProfileImage(Long userId, MultipartFile profileImg) throws IOException {
        User user = getUserById(userId);

        String profileImgUrl = (profileImg == null) ? null : profileImg.isEmpty() ? "" : s3Service.uploadImage(profileImg);

        if (profileImgUrl == null) {
            return;
        } else if (profileImgUrl.isEmpty()) {
            user.updateProfileImg(null);
        } else {
            user.updateProfileImg(profileImgUrl);
        }
    }

    @Override
    @Transactional
    public void updateNickname(Long userId, String nickname) {
        User user = getUserById(userId);

        if (Objects.equals(user.getNickname(), nickname)) {
            return;
        }

        nicknameValidator.validate(nickname);

        if (existsByNickname(nickname)) {
            throw new DPlayException(ResponseError.RESOURCE_ALREADY_EXISTS);
        }
        user.updateNickname(nickname);
    }

    @Override
    public boolean existsByProviderIdAndProvider(String providerId, Platform platform) {
        return userRepository.existsByPlatformIdAndPlatform(providerId, platform);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    @Override
    public User findUserByProviderIdAndProvider(String providerId, Platform platform) {
        return userRepository.findByPlatformIdAndPlatform(providerId, platform).orElseThrow(() -> new DPlayException(ResponseError.USER_NOT_FOUND));
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new DPlayException(ResponseError.USER_NOT_FOUND));
    }

    @Override
    @Transactional
    public User makeUser(String platformId, Platform platform, String nickname, MultipartFile profileImg) throws IOException {
        User user = User.builder()
                .platformId(platformId)
                .platform(platform)
                .nickname(nickname)
                .profileImg((profileImg == null) ? null : s3Service.uploadImage(profileImg)).build();

        return userRepository.save(user);
    }
}
