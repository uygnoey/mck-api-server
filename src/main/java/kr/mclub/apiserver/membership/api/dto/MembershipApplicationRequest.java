package kr.mclub.apiserver.membership.api.dto;

import kr.mclub.apiserver.user.domain.VehicleOwnershipType;

/**
 * 정회원 신청 요청 DTO
 * Membership application request DTO
 */
public record MembershipApplicationRequest(
        String realName,  // 실명
        String phoneNumber,  // 전화번호
        String carNumber,  // 차량번호
        String vinNumber,  // 차대번호
        String carModel,  // 차종
        VehicleOwnershipType ownershipType  // 소유 형태
) {
}
