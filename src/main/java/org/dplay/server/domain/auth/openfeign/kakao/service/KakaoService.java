package org.dplay.server.domain.auth.openfeign.kakao.service;

import lombok.RequiredArgsConstructor;
import org.dplay.server.domain.auth.dto.SocialUserDto;
import org.dplay.server.domain.auth.openfeign.kakao.KakaoFeignClient;
import org.dplay.server.domain.auth.openfeign.kakao.dto.KakaoUserDto;
import org.dplay.server.global.auth.constant.Constant;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;
import org.springframework.stereotype.Service;

public interface KakaoService {
    SocialUserDto getSocialUserInfo(String providerToken);

    void unlinkKakaoUser(final String providerId);
}
