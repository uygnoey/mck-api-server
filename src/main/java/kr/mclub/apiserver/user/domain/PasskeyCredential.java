package kr.mclub.apiserver.user.domain;

import jakarta.persistence.*;
import kr.mclub.apiserver.shared.domain.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Passkey (WebAuthn) 인증 정보
 * Passkey (WebAuthn) credential entity
 */
@Entity
@Table(name = "passkey_credentials")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasskeyCredential extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "credential_id", nullable = false, unique = true, length = 500)
    private String credentialId;  // Base64 인코딩된 Credential ID

    @Column(name = "public_key", nullable = false, columnDefinition = "TEXT")
    private String publicKey;  // 공개키 (COSE 형식, Base64)

    @Column(name = "sign_counter", nullable = false)
    private Long signCounter = 0L;  // 서명 카운터 (재생 공격 방지)

    @Column(name = "transports")
    private String transports;  // 지원 전송: usb, nfc, ble, internal 등 (쉼표로 구분)

    @Column(name = "device_name", length = 100)
    private String deviceName;  // 디바이스 이름 (예: "iPhone 15 Pro")

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Builder
    public PasskeyCredential(String credentialId, String publicKey, Long signCounter,
                             String transports, String deviceName) {
        this.credentialId = credentialId;
        this.publicKey = publicKey;
        this.signCounter = signCounter != null ? signCounter : 0L;
        this.transports = transports;
        this.deviceName = deviceName;
    }

    /**
     * User 설정 (내부 사용)
     * Set user (internal use)
     */
    void setUser(User user) {
        this.user = user;
    }

    /**
     * 서명 카운터 업데이트 (인증 성공 시)
     * Update sign counter (on successful authentication)
     */
    public void updateSignCounter(Long newCounter) {
        this.signCounter = newCounter;
        this.lastUsedAt = LocalDateTime.now();
    }

    /**
     * 서명 카운터 검증 (재생 공격 방지)
     * Verify sign counter to prevent replay attacks
     */
    public boolean verifySignCounter(Long newCounter) {
        // 새 카운터가 현재보다 크면 유효
        return newCounter > this.signCounter;
    }

    /**
     * 비활성화
     * Deactivate credential
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 활성화
     * Activate credential
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * 디바이스 이름 업데이트
     * Update device name
     */
    public void updateDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
