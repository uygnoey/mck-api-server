package kr.mclub.apiserver.membership.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import kr.mclub.apiserver.membership.api.dto.MembershipApplicationRequest;
import kr.mclub.apiserver.membership.api.dto.MembershipApplicationResponse;
import kr.mclub.apiserver.membership.domain.ApplicationStatus;
import kr.mclub.apiserver.membership.service.MembershipApplicationService;
import kr.mclub.apiserver.shared.security.CurrentUser;
import kr.mclub.apiserver.shared.util.ApiResponse;

/**
 * 정회원 신청 Controller / Membership Application Controller
 *
 * <p>정회원 신청서 제출, 조회, 승인/반려 등을 관리하는 REST API를 제공합니다.</p>
 * <p>Provides REST API for membership application submission, retrieval, approval/rejection.</p>
 *
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/membership/applications")
@RequiredArgsConstructor
public class MembershipApplicationController {

    private final MembershipApplicationService applicationService;

    /**
     * 정회원 신청서 제출 / Submit membership application
     *
     * @param userId 사용자 ID (인증된 사용자)
     * @param request 신청 요청 DTO
     * @return 생성된 신청서 응답 DTO
     */
    @PostMapping
    public ApiResponse<MembershipApplicationResponse> submitApplication(
            @CurrentUser Long userId,
            @RequestBody MembershipApplicationRequest request) {
        log.info("정회원 신청서 제출 요청: userId={}, realName={}", userId, request.realName());

        MembershipApplicationResponse response = applicationService.submitApplication(userId, request);

        return ApiResponse.success(response);
    }

    /**
     * 내 신청서 조회 / Get my application
     *
     * @param userId 사용자 ID (인증된 사용자)
     * @return 신청서 응답 DTO (없으면 null)
     */
    @GetMapping("/me")
    public ApiResponse<MembershipApplicationResponse> getMyApplication(@CurrentUser Long userId) {
        log.info("내 신청서 조회: userId={}", userId);

        MembershipApplicationResponse response = applicationService.getApplicationByUserId(userId);

        return ApiResponse.success(response);
    }

    /**
     * 신청서 ID로 조회 (관리자) / Get application by ID (admin)
     *
     * @param applicationId 신청서 ID
     * @return 신청서 응답 DTO
     */
    @GetMapping("/{applicationId}")
    public ApiResponse<MembershipApplicationResponse> getApplicationById(@PathVariable Long applicationId) {
        log.info("신청서 조회: applicationId={}", applicationId);

        MembershipApplicationResponse response = applicationService.getApplicationById(applicationId);

        return ApiResponse.success(response);
    }

    /**
     * 상태별 신청서 목록 조회 (관리자) / Get applications by status (admin)
     *
     * @param status 신청 상태
     * @return 신청서 목록
     */
    @GetMapping
    public ApiResponse<List<MembershipApplicationResponse>> getApplicationsByStatus(
            @RequestParam(required = false) ApplicationStatus status) {
        log.info("상태별 신청서 목록 조회: status={}", status);

        List<MembershipApplicationResponse> responses;
        if (status != null) {
            responses = applicationService.getApplicationsByStatus(status);
        } else {
            // 상태가 지정되지 않으면 빈 리스트 반환 (또는 전체 조회 로직 추가)
            responses = List.of();
        }

        return ApiResponse.success(responses);
    }

    /**
     * 신청서 승인 (관리자) / Approve application (admin)
     *
     * @param applicationId 신청서 ID
     * @param adminId 관리자 ID (인증된 관리자)
     * @return 승인된 신청서 응답 DTO
     */
    @PostMapping("/{applicationId}/approve")
    public ApiResponse<MembershipApplicationResponse> approveApplication(
            @PathVariable Long applicationId,
            @CurrentUser Long adminId) {
        log.info("신청서 승인 요청: applicationId={}, adminId={}", applicationId, adminId);

        MembershipApplicationResponse response = applicationService.approveApplication(applicationId, adminId);

        return ApiResponse.success(response);
    }

    /**
     * 신청서 반려 (관리자) / Reject application (admin)
     *
     * @param applicationId 신청서 ID
     * @param adminId 관리자 ID (인증된 관리자)
     * @param reason 반려 사유
     * @return 반려된 신청서 응답 DTO
     */
    @PostMapping("/{applicationId}/reject")
    public ApiResponse<MembershipApplicationResponse> rejectApplication(
            @PathVariable Long applicationId,
            @CurrentUser Long adminId,
            @RequestParam String reason) {
        log.info("신청서 반려 요청: applicationId={}, adminId={}, reason={}", applicationId, adminId, reason);

        MembershipApplicationResponse response = applicationService.rejectApplication(applicationId, reason, adminId);

        return ApiResponse.success(response);
    }
}
