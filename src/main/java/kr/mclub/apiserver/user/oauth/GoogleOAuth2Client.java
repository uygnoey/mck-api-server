package kr.mclub.apiserver.user.oauth;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import kr.mclub.apiserver.shared.config.OAuth2Properties;
import kr.mclub.apiserver.shared.exception.BusinessException;
import kr.mclub.apiserver.shared.exception.ErrorCode;
import kr.mclub.apiserver.user.domain.OAuthProvider;

/**
 * Google OAuth2 클라이언트
 * Google OAuth2 client implementation
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleOAuth2Client implements OAuth2Client {

    private final OAuth2Properties oAuth2Properties;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public OAuthProvider getProvider() {
        return OAuthProvider.GOOGLE;
    }

    @Override
    public OAuth2TokenResponse exchangeCodeForToken(String code, String redirectUri) {
        OAuth2Properties.ProviderConfig config = getConfig();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", config.getClientId());
        params.add("client_secret", config.getClientSecret());
        params.add("redirect_uri", redirectUri != null ? redirectUri : config.getRedirectUri());
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    config.getTokenUri(),
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            if (body == null) {
                throw new BusinessException(ErrorCode.OAUTH_TOKEN_EXCHANGE_FAILED, "Google token response is empty");
            }

            String accessToken = (String) body.get("access_token");
            String refreshToken = (String) body.get("refresh_token");
            Integer expiresIn = (Integer) body.get("expires_in");
            String tokenType = (String) body.get("token_type");
            String scope = (String) body.get("scope");

            LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiresIn != null ? expiresIn : 3600);

            log.info("Google token exchange successful");

            return new OAuth2TokenResponse(
                    accessToken,
                    refreshToken,
                    expiresIn != null ? expiresIn.longValue() : 3600L,
                    expiresAt,
                    tokenType,
                    scope
            );

        } catch (Exception e) {
            log.error("Google token exchange failed", e);
            throw new BusinessException(ErrorCode.OAUTH_TOKEN_EXCHANGE_FAILED, "Google 토큰 교환 실패: " + e.getMessage());
        }
    }

    @Override
    public OAuth2UserInfo getUserInfo(String accessToken) {
        OAuth2Properties.ProviderConfig config = getConfig();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    config.getUserInfoUri(),
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            if (body == null) {
                throw new BusinessException(ErrorCode.OAUTH_USER_INFO_FAILED, "Google user info response is empty");
            }

            String providerId = (String) body.get("sub");  // Google uses 'sub' as user ID
            String email = (String) body.get("email");
            String name = (String) body.get("name");
            String profileImageUrl = (String) body.get("picture");

            log.info("Google user info fetched: email={}", email);

            return new OAuth2UserInfo(providerId, email, name, profileImageUrl);

        } catch (Exception e) {
            log.error("Google user info fetch failed", e);
            throw new BusinessException(ErrorCode.OAUTH_USER_INFO_FAILED, "Google 사용자 정보 조회 실패: " + e.getMessage());
        }
    }

    private OAuth2Properties.ProviderConfig getConfig() {
        OAuth2Properties.ProviderConfig config = oAuth2Properties.getProvider().get("google");
        if (config == null) {
            throw new BusinessException(ErrorCode.OAUTH_CONFIG_NOT_FOUND, "Google OAuth 설정이 없습니다.");
        }
        return config;
    }
}
