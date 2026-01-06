package kr.mclub.apiserver.membership.api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import kr.mclub.apiserver.user.domain.MemberVehicle;
import kr.mclub.apiserver.user.domain.VehicleOwnershipType;
import kr.mclub.apiserver.user.domain.VehicleStatus;

/**
 * 차량 응답 DTO
 * Vehicle response DTO
 */
public record VehicleResponse(
        Long id,
        Long userId,
        String carNumber,
        String vinNumber,
        String carModel,
        VehicleOwnershipType ownershipType,
        VehicleStatus status,
        LocalDate registeredAt,
        LocalDate soldAt,
        LocalDate gracePeriodEndAt,
        boolean isPrimary,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * 엔티티로부터 응답 DTO 생성
     * Create response DTO from entity
     */
    public static VehicleResponse from(MemberVehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getUserId(),
                vehicle.getCarNumber(),
                vehicle.getVinNumber(),
                vehicle.getCarModel(),
                vehicle.getOwnershipType(),
                vehicle.getStatus(),
                vehicle.getRegisteredAt(),
                vehicle.getSoldAt(),
                vehicle.getGracePeriodEndAt(),
                vehicle.isPrimary(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt()
        );
    }
}
