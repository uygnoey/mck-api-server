package kr.mclub.apiserver.user.domain;

import jakarta.persistence.*;
import kr.mclub.apiserver.shared.domain.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * OAuth 연결 계정
 * OAuth connected account entity (supports multiple providers per user)
 */
@Entity
@Table(name = "oauth_accounts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuthAccount extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OAuthProvider provider;  // GOOGLE, APPLE, NAVER

    @Column(name = "provider_id", nullable = false)
    private String providerId;  // OAuth 제공자의 사용자 ID

    @Column(name = "email")
    private String email;  // OAuth 제공자에서 받은 이메일

    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;  // 암호화된 액세스 토큰

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;  // 암호화된 리프레시 토큰

    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    @Builder
    public OAuthAccount(OAuthProvider provider, String providerId, String email,
                        String accessToken, String refreshToken, LocalDateTime tokenExpiresAt) {
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpiresAt = tokenExpiresAt;
    }

    /**
     * User 설정 (내부 사용)
     * Set user (internal use)
     */
    void setUser(User user) {
        this.user = user;
    }

    /**
     * 토큰 업데이트
     * Update OAuth tokens
     */
    public void updateTokens(String accessToken, String refreshToken, LocalDateTime expiresAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpiresAt = expiresAt;
    }

    /**
     * 토큰 만료 여부 확인
     * Check if token is expired
     */
    public boolean isTokenExpired() {
        return tokenExpiresAt != null && tokenExpiresAt.isBefore(LocalDateTime.now());
    }
}
