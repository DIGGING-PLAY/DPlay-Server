package org.dplay.server.domain.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.dplay.server.domain.auth.service.AuthService;
import org.dplay.server.domain.s3.S3Service;
import org.dplay.server.domain.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthService authService;
    private final S3Service s3Service;

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
        authService.validateNickname(nickname);
        user.updateNickname(nickname);
    }
}
