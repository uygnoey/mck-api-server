package kr.mclub.apiserver.user.oauth;

import java.time.LocalDateTime;

import kr.mclub.apiserver.user.domain.OAuthProvider;

/**
 * OAuth2 클라이언트 인터페이스
 * OAuth2 client interface for exchanging authorization code and fetching user info
 */
public interface OAuth2Client {

    /**
     * 지원하는 OAuth2 제공자
     * Supported OAuth provider
     */
    OAuthProvider getProvider();

    /**
     * Authorization code를 access token으로 교환
     * Exchange authorization code for access token
     *
     * @param code Authorization code from OAuth provider
     * @param redirectUri Redirect URI used in authorization request
     * @return OAuth token response
     */
    OAuth2TokenResponse exchangeCodeForToken(String code, String redirectUri);

    /**
     * Access token으로 사용자 정보 조회
     * Fetch user info using access token
     *
     * @param accessToken Access token from OAuth provider
     * @return OAuth user info
     */
    OAuth2UserInfo getUserInfo(String accessToken);

    /**
     * OAuth2 토큰 응답
     * OAuth2 token response
     */
    record OAuth2TokenResponse(
            String accessToken,
            String refreshToken,
            Long expiresIn,
            LocalDateTime expiresAt,
            String tokenType,
            String scope
    ) {}

    /**
     * OAuth2 사용자 정보
     * OAuth2 user info
     */
    record OAuth2UserInfo(
            String providerId,
            String email,
            String name,
            String profileImageUrl
    ) {}
}
