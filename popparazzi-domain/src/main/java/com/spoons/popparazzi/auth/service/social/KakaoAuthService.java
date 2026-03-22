package com.spoons.popparazzi.auth.service.social;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class KakaoAuthService implements SocialAuthService {

    private static final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

/*    @Override
    public SocialUserInfo getUserInfo(String accessToken) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(USER_INFO_URL, HttpMethod.GET, entity, String.class);

        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
        String id = jsonNode.path("id").asText();
        String email = jsonNode.path("kakao_account").path("email").asText();
        String name = jsonNode.path("properties").path("nickname").asText();

        return new SocialUserInfo(id, email, name, ProviderType.KAKAO);
    }*/
}
