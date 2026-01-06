package kr.mclub.apiserver.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 준회원 상태
 * Associate member status
 */
@Getter
@RequiredArgsConstructor
public enum AssociateStatus {

    PENDING("대기 중"),           // 신규 가입 후 서류 제출 전
    REVIEWING("심사 중"),         // 서류 제출 후 심사 대기
    EXPIRED("만료"),              // 준회원 기간 만료
    REJECTED("반려");             // 가입 반려

    private final String description;
}
