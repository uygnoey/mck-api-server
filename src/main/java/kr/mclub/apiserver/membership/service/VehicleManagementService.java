package kr.mclub.apiserver.membership.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import kr.mclub.apiserver.membership.repository.MembershipPeriodRepository;
import kr.mclub.apiserver.shared.exception.BusinessException;
import kr.mclub.apiserver.shared.exception.ErrorCode;
import kr.mclub.apiserver.user.domain.MemberVehicle;
import kr.mclub.apiserver.user.domain.VehicleOwnershipType;
import kr.mclub.apiserver.user.repository.MemberVehicleRepository;

/**
 * 차량 관리 Service / Vehicle Management Service
 *
 * <p>정회원의 차량 정보 등록 및 관리를 담당합니다.</p>
 * <p>Manages regular member vehicle registration and information.</p>
 *
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehicleManagementService {

    private final MemberVehicleRepository vehicleRepository;
    private final MembershipPeriodRepository periodRepository;

    /**
     * 차량 등록 / Register vehicle
     *
     * @param userId 사용자 ID
     * @param carNumber 차량번호
     * @param vinNumber 차대번호
     * @param carModel 차량 모델
     * @param ownershipType 소유 형태
     * @param isPrimary 주 차량 여부
     * @return 등록된 차량
     * @throws BusinessException 차대번호 중복인 경우
     */
    @Transactional
    public MemberVehicle registerVehicle(Long userId, String carNumber, String vinNumber,
                                         String carModel, VehicleOwnershipType ownershipType,
                                         boolean isPrimary) {
        log.info("차량 등록 시작: userId={}, carNumber={}, vinNumber={}", userId, carNumber, vinNumber);

        // 차대번호 중복 확인
        vehicleRepository.findByVinNumber(vinNumber)
                .ifPresent(existing -> {
                    throw new BusinessException(ErrorCode.DUPLICATE_VIN_NUMBER,
                            "이미 등록된 차대번호입니다: " + vinNumber);
                });

        // 주 차량으로 설정하는 경우, 기존 주 차량 해제
        if (isPrimary) {
            vehicleRepository.findByUserIdAndIsPrimaryTrue(userId)
                    .ifPresent(MemberVehicle::unsetPrimary);
        }

        // 차량 생성
        MemberVehicle vehicle = MemberVehicle.builder()
                .userId(userId)
                .carNumber(carNumber)
                .vinNumber(vinNumber)
                .carModel(carModel)
                .ownershipType(ownershipType)
                .build();

        // 주 차량 설정
        if (isPrimary) {
            vehicle.setAsPrimary();
        }

        MemberVehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("차량 등록 완료: vehicleId={}", savedVehicle.getId());

        return savedVehicle;
    }

    /**
     * 차량 정보 업데이트 / Update vehicle information
     *
     * @param vehicleId 차량 ID
     * @param carNumber 차량번호
     * @param carModel 차량 모델
     * @param ownershipType 소유 형태
     * @return 업데이트된 차량
     * @throws BusinessException 차량을 찾을 수 없는 경우
     */
    @Transactional
    public MemberVehicle updateVehicle(Long vehicleId, String carNumber, String carModel) {
        log.info("차량 정보 업데이트: vehicleId={}", vehicleId);

        MemberVehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VEHICLE_NOT_FOUND));

        vehicle.updateInfo(carNumber, carModel);

        log.info("차량 정보 업데이트 완료: vehicleId={}", vehicleId);
        return vehicle;
    }

    /**
     * 주 차량 설정 / Set primary vehicle
     *
     * @param userId 사용자 ID
     * @param vehicleId 차량 ID
     * @return 주 차량으로 설정된 차량
     * @throws BusinessException 차량을 찾을 수 없거나 권한이 없는 경우
     */
    @Transactional
    public MemberVehicle setPrimaryVehicle(Long userId, Long vehicleId) {
        log.info("주 차량 설정: userId={}, vehicleId={}", userId, vehicleId);

        MemberVehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VEHICLE_NOT_FOUND));

        // 차량 소유자 확인
        if (!vehicle.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인의 차량만 주 차량으로 설정할 수 있습니다");
        }

        // 기존 주 차량 해제
        vehicleRepository.findByUserIdAndIsPrimaryTrue(userId)
                .ifPresent(MemberVehicle::unsetPrimary);

        // 새로운 주 차량 설정
        vehicle.setAsPrimary();

        log.info("주 차량 설정 완료: vehicleId={}", vehicleId);
        return vehicle;
    }

    /**
     * 차량 삭제 / Delete vehicle
     *
     * @param userId 사용자 ID
     * @param vehicleId 차량 ID
     * @throws BusinessException 차량을 찾을 수 없거나 권한이 없는 경우
     */
    @Transactional
    public void deleteVehicle(Long userId, Long vehicleId) {
        log.info("차량 삭제: userId={}, vehicleId={}", userId, vehicleId);

        MemberVehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VEHICLE_NOT_FOUND));

        // 차량 소유자 확인
        if (!vehicle.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인의 차량만 삭제할 수 있습니다");
        }

        vehicleRepository.delete(vehicle);
        log.info("차량 삭제 완료: vehicleId={}", vehicleId);
    }

    /**
     * 사용자의 모든 차량 조회 / Get all user vehicles
     *
     * @param userId 사용자 ID
     * @return 차량 목록
     */
    public List<MemberVehicle> getUserVehicles(Long userId) {
        return vehicleRepository.findByUserIdOrderByIsPrimaryDescRegisteredAtDesc(userId);
    }

    /**
     * 사용자의 주 차량 조회 / Get user's primary vehicle
     *
     * @param userId 사용자 ID
     * @return 주 차량 (없으면 null)
     */
    public MemberVehicle getPrimaryVehicle(Long userId) {
        return vehicleRepository.findByUserIdAndIsPrimaryTrue(userId)
                .orElse(null);
    }

    /**
     * 차량 ID로 조회 / Get vehicle by ID
     *
     * @param vehicleId 차량 ID
     * @return 차량
     * @throws BusinessException 차량을 찾을 수 없는 경우
     */
    public MemberVehicle getVehicleById(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.VEHICLE_NOT_FOUND));
    }

    /**
     * 차대번호로 조회 / Get vehicle by VIN number
     *
     * @param vinNumber 차대번호
     * @return 차량 (없으면 null)
     */
    public MemberVehicle getVehicleByVin(String vinNumber) {
        return vehicleRepository.findByVinNumber(vinNumber)
                .orElse(null);
    }

    /**
     * 차량 소유 확인 / Verify vehicle ownership
     *
     * @param userId 사용자 ID
     * @param vehicleId 차량 ID
     * @return 소유 여부
     */
    public boolean isVehicleOwner(Long userId, Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .map(vehicle -> vehicle.getUserId().equals(userId))
                .orElse(false);
    }

    /**
     * 활성 차량 확인 (유예 기간 포함)
     * Check if vehicle is active (including grace period)
     *
     * @param userId 사용자 ID
     * @return 활성 차량 존재 여부
     */
    public boolean hasActiveVehicle(Long userId) {
        // 활성 차량이 있으면 true
        return vehicleRepository.hasActiveVehicle(userId);
    }

    /**
     * 유예 기간 차량 개수 확인
     * Get count of vehicles in grace period
     *
     * @param userId 사용자 ID
     * @return 유예 기간 차량 개수
     */
    public long getGracePeriodVehicleCount(Long userId) {
        return vehicleRepository.findByUserIdOrderByIsPrimaryDescRegisteredAtDesc(userId).stream()
                .filter(vehicle -> vehicle.getStatus() == kr.mclub.apiserver.user.domain.VehicleStatus.GRACE_PERIOD)
                .count();
    }
}
