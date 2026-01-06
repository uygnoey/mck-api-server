/**
 * Shared Kernel - 공유 커널 모듈
 *
 * <p>모든 모듈이 의존할 수 있는 공통 기능을 제공합니다.</p>
 *
 * <h2>주요 기능</h2>
 * <ul>
 *   <li>Domain - 공통 도메인 객체 (BaseEntity, DomainEvent)</li>
 *   <li>Exception - 공통 예외 처리</li>
 *   <li>Security - 보안 설정 및 인증/인가</li>
 *   <li>Util - 공통 유틸리티 (ApiResponse, PageResponse)</li>
 *   <li>Config - 공통 설정 (JPA, WebMvc)</li>
 * </ul>
 *
 * @since 1.0
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Shared Kernel"
)
package kr.mclub.apiserver.shared;