package org.dplay.server.domain.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.dplay.server.domain.auth.service.AuthService;
import org.dplay.server.domain.user.entity.User;
import org.dplay.server.domain.user.repository.UserRepository;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthService authService;
    private final UserRepository userRepository;

    @Transactional
    public void updateProfileImage(final Long userId, final String profileImg) {
        User user = getUserById(userId);

        if (profileImg == null) {
            return;
        } else if (profileImg.isEmpty()) {
            user.updateProfileImg(null);
        } else {
            user.updateProfileImg(profileImg);
        }
    }

    @Transactional
    public void updateNickname(final Long userId, final String nickname) {
        authService.validateNickname(nickname);
        User user = getUserById(userId);
        user.updateNickname(nickname);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new DPlayException(ResponseError.USER_NOT_FOUND));
    }
}
