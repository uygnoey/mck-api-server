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
 * Apple OAuth2 클라이언트
 * Apple OAuth2 client implementation (Sign in with Apple)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppleOAuth2Client implements OAuth2Client {

    private final OAuth2Properties oAuth2Properties;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public OAuthProvider getProvider() {
        return OAuthProvider.APPLE;
    }

    @Override
    public OAuth2TokenResponse exchangeCodeForToken(String code, String redirectUri) {
        OAuth2Properties.ProviderConfig config = getConfig();

        // Apple requires client_secret to be a JWT signed with private key
        // For now, we expect client_secret to be pre-generated and configured
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
                throw new BusinessException(ErrorCode.OAUTH_TOKEN_EXCHANGE_FAILED, "Apple token response is empty");
            }

            String accessToken = (String) body.get("access_token");
            String refreshToken = (String) body.get("refresh_token");
            Integer expiresIn = (Integer) body.get("expires_in");
            String tokenType = (String) body.get("token_type");
            String idToken = (String) body.get("id_token");  // Apple returns ID token with user info

            LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiresIn != null ? expiresIn : 3600);

            log.info("Apple token exchange successful");

            return new OAuth2TokenResponse(
                    accessToken,
                    refreshToken,
                    expiresIn != null ? expiresIn.longValue() : 3600L,
                    expiresAt,
                    tokenType,
                    null
            );

        } catch (Exception e) {
            log.error("Apple token exchange failed", e);
            throw new BusinessException(ErrorCode.OAUTH_TOKEN_EXCHANGE_FAILED, "Apple 토큰 교환 실패: " + e.getMessage());
        }
    }

    @Override
    public OAuth2UserInfo getUserInfo(String accessToken) {
        // Apple doesn't provide a user info endpoint
        // User info is included in the ID token during token exchange
        // For now, we'll throw an exception and handle user info extraction from ID token separately
        throw new BusinessException(
                ErrorCode.NOT_IMPLEMENTED,
                "Apple은 ID Token에서 사용자 정보를 추출해야 합니다. " +
                        "클라이언트에서 ID Token을 파싱하거나 Authorization Code와 함께 사용자 정보를 전달해주세요."
        );
    }

    private OAuth2Properties.ProviderConfig getConfig() {
        OAuth2Properties.ProviderConfig config = oAuth2Properties.getProvider().get("apple");
        if (config == null) {
            throw new BusinessException(ErrorCode.OAUTH_CONFIG_NOT_FOUND, "Apple OAuth 설정이 없습니다.");
        }
        return config;
    }
}
