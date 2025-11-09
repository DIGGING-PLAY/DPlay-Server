package org.dplay.server.domain.auth.openfeign.kakao.service;

import lombok.RequiredArgsConstructor;
import org.dplay.server.domain.auth.dto.SocialUserDto;
import org.dplay.server.domain.auth.openfeign.kakao.KakaoFeignClient;
import org.dplay.server.domain.auth.openfeign.kakao.dto.KakaoUserDto;
import org.dplay.server.global.auth.constant.Constant;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoService {

    private final KakaoFeignClient kakaoFeignClient;
    public SocialUserDto getSocialUserInfo(String providerToken) {
        KakaoUserDto kakaoUserDto = kakaoFeignClient.getUserInformation(Constant.BEARER_TOKEN_PREFIX + providerToken);
        return SocialUserDto.of(
                kakaoUserDto.id().toString(),
                kakaoUserDto.kakaoAccount().email());
    }
}
