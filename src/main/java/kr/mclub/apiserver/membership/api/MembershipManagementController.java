package kr.mclub.apiserver.membership.api;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import kr.mclub.apiserver.membership.api.dto.DocumentResponse;
import kr.mclub.apiserver.membership.api.dto.DocumentUploadRequest;
import kr.mclub.apiserver.membership.api.dto.MembershipPeriodResponse;
import kr.mclub.apiserver.membership.api.dto.OcrResultResponse;
import kr.mclub.apiserver.membership.api.dto.VehicleResponse;
import kr.mclub.apiserver.membership.domain.MembershipPeriod;
import kr.mclub.apiserver.membership.service.DocumentVerificationService;
import kr.mclub.apiserver.membership.service.MembershipRenewalService;
import kr.mclub.apiserver.membership.service.VehicleManagementService;
import kr.mclub.apiserver.shared.security.CurrentUser;
import kr.mclub.apiserver.shared.util.ApiResponse;
import kr.mclub.apiserver.user.domain.MemberVehicle;
import kr.mclub.apiserver.user.domain.VehicleOwnershipType;

/**
 * 멤버십 관리 Controller / Membership Management Controller
 *
 * <p>서류 관리, 차량 관리, 멤버십 갱신 등을 관리하는 REST API를 제공합니다.</p>
 * <p>Provides REST API for document management, vehicle management, and membership renewal.</p>
 *
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/membership/management")
@RequiredArgsConstructor
public class MembershipManagementController {

    private final DocumentVerificationService documentService;
    private final VehicleManagementService vehicleService;
    private final MembershipRenewalService renewalService;

    // ================== 서류 관리 / Document Management ==================

    /**
     * 서류 업로드 등록 / Register document upload
     *
     * @param applicationId 신청서 ID
     * @param request 서류 업로드 요청 DTO
     * @return 등록된 서류 응답 DTO
     */
    @PostMapping("/applications/{applicationId}/documents")
    public ApiResponse<DocumentResponse> uploadDocument(
            @PathVariable Long applicationId,
            @RequestBody DocumentUploadRequest request) {
        log.info("서류 업로드 등록: applicationId={}, documentType={}", applicationId, request.documentType());

        DocumentResponse response = documentService.uploadDocument(applicationId, request);

        return ApiResponse.success(response);
    }

    /**
     * 신청서의 모든 서류 조회 / Get all documents for application
     *
     * @param applicationId 신청서 ID
     * @return 서류 목록
     */
    @GetMapping("/applications/{applicationId}/documents")
    public ApiResponse<List<DocumentResponse>> getDocumentsByApplicationId(@PathVariable Long applicationId) {
        log.info("신청서 서류 목록 조회: applicationId={}", applicationId);

        List<DocumentResponse> responses = documentService.getDocumentsByApplicationId(applicationId);

        return ApiResponse.success(responses);
    }

    /**
     * 서류 검증 승인 (관리자) / Approve document verification (admin)
     *
     * @param documentId 서류 ID
     * @param verifierId 검증자 ID (인증된 관리자)
     * @return 검증된 서류 응답 DTO
     */
    @PostMapping("/documents/{documentId}/approve")
    public ApiResponse<DocumentResponse> approveDocument(
            @PathVariable Long documentId,
            @CurrentUser Long verifierId) {
        log.info("서류 검증 승인: documentId={}, verifierId={}", documentId, verifierId);

        DocumentResponse response = documentService.approveDocument(documentId, verifierId);

        return ApiResponse.success(response);
    }

    /**
     * 서류 검증 반려 (관리자) / Reject document verification (admin)
     *
     * @param documentId 서류 ID
     * @param verifierId 검증자 ID (인증된 관리자)
     * @param reason 반려 사유
     * @return 반려된 서류 응답 DTO
     */
    @PostMapping("/documents/{documentId}/reject")
    public ApiResponse<DocumentResponse> rejectDocument(
            @PathVariable Long documentId,
            @CurrentUser Long verifierId,
            @RequestParam String reason) {
        log.info("서류 검증 반려: documentId={}, verifierId={}, reason={}", documentId, verifierId, reason);

        DocumentResponse response = documentService.rejectDocument(documentId, reason, verifierId);

        return ApiResponse.success(response);
    }

    /**
     * 서류의 OCR 결과 조회 / Get OCR result for document
     *
     * @param documentId 서류 ID
     * @return OCR 결과 응답 DTO
     */
    @GetMapping("/documents/{documentId}/ocr")
    public ApiResponse<OcrResultResponse> getOcrResult(@PathVariable Long documentId) {
        log.info("OCR 결과 조회: documentId={}", documentId);

        OcrResultResponse response = documentService.getOcrResult(documentId);

        return ApiResponse.success(response);
    }

    /**
     * OCR 재처리 요청 (관리자) / Request OCR reprocessing (admin)
     *
     * @param documentId 서류 ID
     * @return OCR 결과 응답 DTO
     */
    @PostMapping("/documents/{documentId}/ocr/reprocess")
    public ApiResponse<OcrResultResponse> reprocessOcr(@PathVariable Long documentId) {
        log.info("OCR 재처리: documentId={}", documentId);

        OcrResultResponse response = documentService.reprocessOcr(documentId);

        return ApiResponse.success(response);
    }

    // ================== 차량 관리 / Vehicle Management ==================

    /**
     * 차량 등록 / Register vehicle
     *
     * @param userId 사용자 ID (인증된 사용자)
     * @param carNumber 차량번호
     * @param vinNumber 차대번호
     * @param carModel 차량 모델
     * @param ownershipType 소유 형태
     * @param isPrimary 주 차량 여부
     * @return 등록된 차량 응답 DTO
     */
    @PostMapping("/vehicles")
    public ApiResponse<VehicleResponse> registerVehicle(
            @CurrentUser Long userId,
            @RequestParam String carNumber,
            @RequestParam String vinNumber,
            @RequestParam String carModel,
            @RequestParam VehicleOwnershipType ownershipType,
            @RequestParam(defaultValue = "false") boolean isPrimary) {
        log.info("차량 등록: userId={}, carNumber={}, vinNumber={}", userId, carNumber, vinNumber);

        MemberVehicle vehicle = vehicleService.registerVehicle(userId, carNumber, vinNumber, carModel, ownershipType, isPrimary);

        return ApiResponse.success(VehicleResponse.from(vehicle));
    }

    /**
     * 내 차량 목록 조회 / Get my vehicles
     *
     * @param userId 사용자 ID (인증된 사용자)
     * @return 차량 목록
     */
    @GetMapping("/vehicles/me")
    public ApiResponse<List<VehicleResponse>> getMyVehicles(@CurrentUser Long userId) {
        log.info("내 차량 목록 조회: userId={}", userId);

        List<VehicleResponse> responses = vehicleService.getUserVehicles(userId).stream()
                .map(VehicleResponse::from)
                .toList();

        return ApiResponse.success(responses);
    }

    /**
     * 차량 정보 업데이트 / Update vehicle information
     *
     * @param vehicleId 차량 ID
     * @param carNumber 차량번호
     * @param carModel 차량 모델
     * @return 업데이트된 차량 응답 DTO
     */
    @PutMapping("/vehicles/{vehicleId}")
    public ApiResponse<VehicleResponse> updateVehicle(
            @PathVariable Long vehicleId,
            @RequestParam String carNumber,
            @RequestParam String carModel) {
        log.info("차량 정보 업데이트: vehicleId={}", vehicleId);

        MemberVehicle vehicle = vehicleService.updateVehicle(vehicleId, carNumber, carModel);

        return ApiResponse.success(VehicleResponse.from(vehicle));
    }

    /**
     * 주 차량 설정 / Set primary vehicle
     *
     * @param userId 사용자 ID (인증된 사용자)
     * @param vehicleId 차량 ID
     * @return 주 차량으로 설정된 차량 응답 DTO
     */
    @PostMapping("/vehicles/{vehicleId}/primary")
    public ApiResponse<VehicleResponse> setPrimaryVehicle(
            @CurrentUser Long userId,
            @PathVariable Long vehicleId) {
        log.info("주 차량 설정: userId={}, vehicleId={}", userId, vehicleId);

        MemberVehicle vehicle = vehicleService.setPrimaryVehicle(userId, vehicleId);

        return ApiResponse.success(VehicleResponse.from(vehicle));
    }

    /**
     * 차량 삭제 / Delete vehicle
     *
     * @param userId 사용자 ID (인증된 사용자)
     * @param vehicleId 차량 ID
     * @return 성공 응답
     */
    @DeleteMapping("/vehicles/{vehicleId}")
    public ApiResponse<Void> deleteVehicle(
            @CurrentUser Long userId,
            @PathVariable Long vehicleId) {
        log.info("차량 삭제: userId={}, vehicleId={}", userId, vehicleId);

        vehicleService.deleteVehicle(userId, vehicleId);

        return ApiResponse.success(null);
    }

    // ================== 멤버십 갱신 / Membership Renewal ==================

    /**
     * 내 활성 멤버십 기간 조회 / Get my active membership period
     *
     * @param userId 사용자 ID (인증된 사용자)
     * @return 활성 멤버십 기간 응답 DTO
     */
    @GetMapping("/periods/me/active")
    public ApiResponse<MembershipPeriodResponse> getMyActivePeriod(@CurrentUser Long userId) {
        log.info("내 활성 멤버십 조회: userId={}", userId);

        MembershipPeriod period = renewalService.getActivePeriod(userId);

        return ApiResponse.success(period != null ? MembershipPeriodResponse.from(period) : null);
    }

    /**
     * 내 모든 멤버십 기간 조회 / Get all my membership periods
     *
     * @param userId 사용자 ID (인증된 사용자)
     * @return 멤버십 기간 목록
     */
    @GetMapping("/periods/me")
    public ApiResponse<List<MembershipPeriodResponse>> getMyAllPeriods(@CurrentUser Long userId) {
        log.info("내 모든 멤버십 기간 조회: userId={}", userId);

        List<MembershipPeriodResponse> responses = renewalService.getAllPeriods(userId).stream()
                .map(MembershipPeriodResponse::from)
                .toList();

        return ApiResponse.success(responses);
    }

    /**
     * 멤버십 갱신 가능 여부 확인 / Check if membership renewal is available
     *
     * @param userId 사용자 ID (인증된 사용자)
     * @param targetYear 갱신할 년도
     * @return 갱신 가능 여부
     */
    @GetMapping("/periods/can-renew")
    public ApiResponse<Boolean> canRenew(
            @CurrentUser Long userId,
            @RequestParam Integer targetYear) {
        log.info("멤버십 갱신 가능 여부 확인: userId={}, targetYear={}", userId, targetYear);

        boolean canRenew = renewalService.canRenew(userId, targetYear);

        return ApiResponse.success(canRenew);
    }

    /**
     * 초기 멤버십 기간 생성 (관리자) / Create initial membership period (admin)
     *
     * @param userId 사용자 ID
     * @param paymentId 가입비 결제 ID
     * @return 생성된 멤버십 기간 응답 DTO
     */
    @PostMapping("/periods/initial")
    public ApiResponse<MembershipPeriodResponse> createInitialPeriod(
            @RequestParam Long userId,
            @RequestParam Long paymentId) {
        log.info("초기 멤버십 기간 생성: userId={}, paymentId={}", userId, paymentId);

        MembershipPeriod period = renewalService.createInitialPeriod(userId, paymentId);

        return ApiResponse.success(MembershipPeriodResponse.from(period));
    }

    /**
     * 멤버십 갱신 (관리자) / Renew membership (admin)
     *
     * @param userId 사용자 ID
     * @param paymentId 연회비 결제 ID
     * @return 갱신된 멤버십 기간 응답 DTO
     */
    @PostMapping("/periods/renew")
    public ApiResponse<MembershipPeriodResponse> renewMembership(
            @RequestParam Long userId,
            @RequestParam Long paymentId) {
        log.info("멤버십 갱신: userId={}, paymentId={}", userId, paymentId);

        MembershipPeriod period = renewalService.renewMembership(userId, paymentId);

        return ApiResponse.success(MembershipPeriodResponse.from(period));
    }
}
