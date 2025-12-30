/**
 * User Module - 사용자 관리 모듈
 *
 * <p>사용자 등록, 인증, 프로필 관리, 등급 관리 기능을 제공합니다.</p>
 *
 * <h2>주요 기능</h2>
 * <ul>
 *   <li>OAuth2 소셜 로그인 (Google, Apple, Naver)</li>
 *   <li>Passkey (WebAuthn) 인증</li>
 *   <li>JWT 토큰 관리</li>
 *   <li>사용자 프로필 관리</li>
 *   <li>등급 관리 (동적 등급 시스템)</li>
 *   <li>차량 관리</li>
 * </ul>
 *
 * <h2>Published Events</h2>
 * <ul>
 *   <li>{@code UserRegisteredEvent} - 사용자 등록 시</li>
 *   <li>{@code UserGradeChangedEvent} - 등급 변경 시</li>
 *   <li>{@code UserWithdrawnEvent} - 회원 탈퇴 시</li>
 * </ul>
 *
 * @since 1.0
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "User Module",
        allowedDependencies = {"shared", "shared::domain", "shared::exception", "shared::util", "shared::security"}
)
package kr.mclub.apiserver.user;
