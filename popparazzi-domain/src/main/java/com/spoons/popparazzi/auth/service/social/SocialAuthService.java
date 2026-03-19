package com.spoons.popparazzi.auth.service.social;

import com.nimbusds.jose.JOSEException;
import kr.co.hs.domain.modules.memberAuth.infrastructure.dto.request.SocialUserInfo;

import java.io.IOException;
import java.text.ParseException;

public interface SocialAuthService {

    SocialUserInfo getUserInfo(String accessToken) throws IOException, ParseException, JOSEException;
}
