package org.dplay.server.domain.auth.openfeign.apple.service;

import org.dplay.server.domain.auth.dto.SocialUserDto;

public interface AppleService {

    SocialUserDto getSocialUserInfo(String identityToken);
}
