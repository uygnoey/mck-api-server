package kr.mclub.apiserver.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 차량 상태
 * Vehicle status
 */
@Getter
@RequiredArgsConstructor
public enum VehicleStatus {

    ACTIVE("활성"),          // 현재 소유 중
    SOLD("매각"),            // 매각됨
    GRACE_PERIOD("유예기간"); // M차량 없을 때 1년 유예

    private final String description;
}
