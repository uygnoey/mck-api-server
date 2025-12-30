package kr.mclub.apiserver.user.repository;

import kr.mclub.apiserver.user.domain.OAuthAccount;
import kr.mclub.apiserver.user.domain.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * OAuth 계정 Repository
 * OAuth account repository
 */
public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {

    /**
     * 제공자와 제공자 ID로 조회
     * Find by provider and provider ID
     */
    Optional<OAuthAccount> findByProviderAndProviderId(OAuthProvider provider, String providerId);

    /**
     * 사용자 ID로 조회
     * Find all accounts by user ID
     */
    List<OAuthAccount> findByUserId(Long userId);

    /**
     * 사용자 ID와 제공자로 조회
     * Find by user ID and provider
     */
    Optional<OAuthAccount> findByUserIdAndProvider(Long userId, OAuthProvider provider);

    /**
     * 제공자와 제공자 ID 존재 여부
     * Check if provider and provider ID exists
     */
    boolean existsByProviderAndProviderId(OAuthProvider provider, String providerId);

    /**
     * 사용자의 OAuth 계정 수
     * Count OAuth accounts for user
     */
    long countByUserId(Long userId);

    /**
     * 사용자 ID와 제공자로 삭제
     * Delete by user ID and provider
     */
    void deleteByUserIdAndProvider(Long userId, OAuthProvider provider);
}
