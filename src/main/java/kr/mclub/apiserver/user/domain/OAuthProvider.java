package kr.mclub.apiserver.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * OAuth 제공자
 * OAuth providers for social login
 */
@Getter
@RequiredArgsConstructor
public enum OAuthProvider {

    GOOGLE("Google"),
    APPLE("Apple"),
    NAVER("Naver");

    private final String displayName;
}
