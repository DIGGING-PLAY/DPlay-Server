package org.dplay.server.domain.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.dplay.server.domain.auth.service.AuthService;
import org.dplay.server.domain.s3.S3Service;
import org.dplay.server.domain.user.UserRetriever;
import org.dplay.server.domain.user.entity.User;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;
import org.dplay.server.global.util.NicknameValidator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthService authService;
    private final S3Service s3Service;
    private final NicknameValidator nicknameValidator;
    private final UserRetriever userRetriever;

    @Transactional
    public void updateProfileImage(final String accessToken, final MultipartFile profileImg) throws IOException {
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

    @Transactional
    public void updateNickname(final String accessToken, final String nickname) {
        User user = authService.getUserFromToken(accessToken);

        if (Objects.equals(user.getNickname(), nickname)) {
            return;
        } else if (userRetriever.existsByNickname(nickname)) {
            throw new DPlayException(ResponseError.RESOURCE_ALREADY_EXISTS);
        } else {
            nicknameValidator.validate(nickname);
            user.updateNickname(nickname);
        }
    }
}
