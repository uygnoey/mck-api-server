package kr.mclub.apiserver.user.repository;

import kr.mclub.apiserver.user.domain.PasskeyCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Passkey 인증 정보 Repository
 * Passkey credential repository
 */
public interface PasskeyCredentialRepository extends JpaRepository<PasskeyCredential, Long> {

    /**
     * Credential ID로 조회
     * Find by credential ID
     */
    Optional<PasskeyCredential> findByCredentialId(String credentialId);

    /**
     * 사용자 ID로 활성 Passkey 목록 조회
     * Find active passkeys by user ID
     */
    List<PasskeyCredential> findByUserIdAndIsActiveTrue(Long userId);

    /**
     * 사용자 ID로 모든 Passkey 조회
     * Find all passkeys by user ID
     */
    List<PasskeyCredential> findByUserId(Long userId);

    /**
     * Credential ID 존재 여부
     * Check if credential ID exists
     */
    boolean existsByCredentialId(String credentialId);

    /**
     * 사용자의 활성 Passkey 수
     * Count active passkeys for user
     */
    long countByUserIdAndIsActiveTrue(Long userId);
}
