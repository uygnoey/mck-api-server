package kr.mclub.apiserver.user.oauth;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import kr.mclub.apiserver.shared.exception.BusinessException;
import kr.mclub.apiserver.shared.exception.ErrorCode;
import kr.mclub.apiserver.user.domain.OAuthProvider;

/**
 * OAuth2 클라이언트 팩토리
 * OAuth2 client factory to get appropriate client by provider
 */
@Component
@RequiredArgsConstructor
public class OAuth2ClientFactory {

    private final List<OAuth2Client> oAuth2Clients;

    /**
     * Provider에 맞는 OAuth2 클라이언트 반환
     * Get OAuth2 client by provider
     *
     * @param provider OAuth provider (GOOGLE, NAVER, APPLE)
     * @return OAuth2 client implementation
     */
    public OAuth2Client getClient(OAuthProvider provider) {
        Map<OAuthProvider, OAuth2Client> clientMap = oAuth2Clients.stream()
                .collect(Collectors.toMap(OAuth2Client::getProvider, Function.identity()));

        OAuth2Client client = clientMap.get(provider);
        if (client == null) {
            throw new BusinessException(
                    ErrorCode.OAUTH_PROVIDER_NOT_SUPPORTED,
                    "지원하지 않는 OAuth 제공자입니다: " + provider
            );
        }

        return client;
    }
}
