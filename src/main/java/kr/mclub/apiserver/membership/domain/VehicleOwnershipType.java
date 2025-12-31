package kr.mclub.apiserver.membership.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 차량 소유 형태 / Vehicle Ownership Type
 *
 * <p>정회원 가입 시 차량의 소유/이용 형태를 구분합니다.</p>
 *
 * @since 1.0
 */
@Getter
@RequiredArgsConstructor
public enum VehicleOwnershipType {

    /**
     * 개인 소유
     */
    PERSONAL("개인"),

    /**
     * 법인 소유
     */
    CORPORATE("법인"),

    /**
     * 개인 리스
     */
    LEASE("리스"),

    /**
     * 개인 렌트
     */
    RENTAL("렌트"),

    /**
     * 법인 리스
     */
    CORPORATE_LEASE("법인 리스"),

    /**
     * 법인 렌트
     */
    CORPORATE_RENTAL("법인 렌트");

    /**
     * 한글 표기명
     */
    private final String displayName;
}