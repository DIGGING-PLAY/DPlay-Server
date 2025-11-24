package org.dplay.server.domain.auth.openfeign.kakao.service.impl;

import lombok.RequiredArgsConstructor;
import org.dplay.server.domain.auth.dto.SocialUserDto;
import org.dplay.server.domain.auth.openfeign.kakao.KakaoFeignClient;
import org.dplay.server.domain.auth.openfeign.kakao.dto.KakaoUserDto;
import org.dplay.server.domain.auth.openfeign.kakao.service.KakaoService;
import org.dplay.server.global.auth.constant.Constant;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoServiceImpl implements KakaoService {

    private final KakaoFeignClient kakaoFeignClient;

    @Value("${oauth.kakao.admin-key}")
    private String adminKey;

    @Override
    public SocialUserDto getSocialUserInfo(String providerToken) {
        try {
            KakaoUserDto kakaoUserDto = kakaoFeignClient.getUserInformation(Constant.BEARER_TOKEN_PREFIX + providerToken);
            return SocialUserDto.of(
                    kakaoUserDto.id().toString()
            );
        } catch (Exception e) {
            throw new DPlayException(ResponseError.INVALID_TOKEN);
        }


    }

    @Override
    public void unlinkKakaoUser(String providerId) {
        kakaoFeignClient.unlinkUser(
                "kakaoAK" + adminKey,
                "user_id",
                Long.valueOf(providerId)
        );
    }
}
