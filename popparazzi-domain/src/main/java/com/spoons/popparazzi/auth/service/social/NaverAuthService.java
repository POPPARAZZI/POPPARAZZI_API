package com.spoons.popparazzi.auth.service.social;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.hs.domain.modules.member.domain.constant.ProviderType;
import kr.co.hs.domain.modules.memberAuth.infrastructure.dto.request.SocialUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class NaverAuthService implements SocialAuthService{

    private static final String USER_INFO_URL = "https://openapi.naver.com/v1/nid/me";

    @Override
    public SocialUserInfo getUserInfo(String accessToken) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(USER_INFO_URL, HttpMethod.GET, entity, String.class);

        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody()).path("response");
        String id = jsonNode.path("id").asText();
        String email = jsonNode.path("email").asText();
        String name = jsonNode.path("name").asText();

        return new SocialUserInfo(id, email, name, ProviderType.NAVER);
    }
}
