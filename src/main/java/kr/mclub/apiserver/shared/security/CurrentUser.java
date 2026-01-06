package kr.mclub.apiserver.shared.security;

import java.lang.annotation.*;

/**
 * 현재 로그인한 사용자 ID를 주입받는 어노테이션
 * Annotation to inject the current authenticated user ID
 *
 * <pre>
 * Usage:
 * {@code @GetMapping("/me")}
 * {@code public UserResponse getMyProfile(@CurrentUser Long userId) { ... }}
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}
