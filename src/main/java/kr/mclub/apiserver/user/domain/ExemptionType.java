package kr.mclub.apiserver.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 연회비 면제 유형
 * Annual fee exemption type
 */
@Getter
@RequiredArgsConstructor
public enum ExemptionType {

    NONE("면제 없음"),              // 면제 없음
    PERMANENT("영구 면제"),         // 영구 면제 (고문, 명예정회원 등)
    ONE_TIME("1회성 면제");         // 1회성 면제 (특정 연도만)

    private final String description;
}
