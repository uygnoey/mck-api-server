package kr.mclub.apiserver.membership.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import kr.mclub.apiserver.membership.api.dto.MembershipApplicationRequest;
import kr.mclub.apiserver.membership.api.dto.MembershipApplicationResponse;
import kr.mclub.apiserver.membership.domain.ApplicationStatus;
import kr.mclub.apiserver.membership.domain.MembershipApplication;
import kr.mclub.apiserver.membership.event.MembershipApplicationApprovedEvent;
import kr.mclub.apiserver.membership.event.MembershipApplicationSubmittedEvent;
import kr.mclub.apiserver.membership.repository.MembershipApplicationRepository;
import kr.mclub.apiserver.shared.exception.BusinessException;
import kr.mclub.apiserver.shared.exception.ErrorCode;

/**
 * 정회원 신청 Service / Membership Application Service
 *
 * 정회원 신청서 제출, 조회, 승인/반려 등을 관리합니다.
 * Manages membership application submission, retrieval, approval/rejection
 *
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MembershipApplicationService {

    private final MembershipApplicationRepository applicationRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 정회원 신청서 제출 / Submit membership application
     *
     * @param userId 사용자 ID
     * @param request 신청 요청 DTO
     * @return 생성된 신청서 응답 DTO
     * @throws BusinessException 이미 대기중인 신청서가 있는 경우
     */
    @Transactional
    public MembershipApplicationResponse submitApplication(Long userId, MembershipApplicationRequest request) {
        log.info("정회원 신청 제출 시작: userId={}, realName={}", userId, request.realName());

        // 기존 대기중인 신청서 확인 / Check for existing pending application
        applicationRepository.findByUserIdAndStatus(userId, ApplicationStatus.DOCUMENT_PENDING)
                .ifPresent(existing -> {
                    throw new BusinessException(ErrorCode.APPLICATION_ALREADY_EXISTS,
                            "이미 심사 대기중인 신청서가 있습니다. applicationId=" + existing.getId());
                });

        // 신청서 생성 / Create application
        MembershipApplication application = MembershipApplication.builder()
                .userId(userId)
                .applicationNumber(generateApplicationNumber())
                .applicantName(request.realName())
                .applicantPhone(request.phoneNumber())
                .carNumber(request.carNumber())
                .vinNumber(request.vinNumber())
                .carModel(request.carModel())
                .vehicleOwnershipType(request.ownershipType())
                .build();

        MembershipApplication savedApplication = applicationRepository.save(application);

        // 도메인 이벤트 발행 / Publish domain event
        eventPublisher.publishEvent(MembershipApplicationSubmittedEvent.of(
                savedApplication.getId(),
                savedApplication.getUserId(),
                savedApplication.getApplicantName()
        ));

        log.info("정회원 신청 제출 완료: applicationId={}", savedApplication.getId());

        return MembershipApplicationResponse.from(savedApplication);
    }

    /**
     * 사용자의 신청서 조회 / Get user's application status
     *
     * @param userId 사용자 ID
     * @return 신청서 응답 DTO (없으면 null)
     */
    public MembershipApplicationResponse getApplicationByUserId(Long userId) {
        return applicationRepository.findByUserId(userId)
                .map(MembershipApplicationResponse::from)
                .orElse(null);
    }

    /**
     * 신청서 ID로 조회 / Get application by ID
     *
     * @param applicationId 신청서 ID
     * @return 신청서 응답 DTO
     * @throws BusinessException 신청서를 찾을 수 없는 경우
     */
    public MembershipApplicationResponse getApplicationById(Long applicationId) {
        MembershipApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

        return MembershipApplicationResponse.from(application);
    }

    /**
     * 상태별 신청서 목록 조회 / Get applications by status
     *
     * @param status 신청 상태
     * @return 신청서 목록
     */
    public List<MembershipApplicationResponse> getApplicationsByStatus(ApplicationStatus status) {
        return applicationRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(MembershipApplicationResponse::from)
                .toList();
    }

    /**
     * 신청서 승인 / Approve application
     *
     * 신청서를 승인하고 승인 이벤트를 발행합니다.
     * Approves application and publishes approval event.
     *
     * @param applicationId 신청서 ID
     * @param adminId 승인하는 관리자 ID
     * @return 승인된 신청서 응답 DTO
     * @throws BusinessException 신청서를 찾을 수 없거나 이미 처리된 경우
     */
    @Transactional
    public MembershipApplicationResponse approveApplication(Long applicationId, Long adminId) {
        log.info("정회원 신청 승인 시작: applicationId={}, adminId={}", applicationId, adminId);

        MembershipApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

        // 승인 가능 상태 확인 / Check if approvable
        if (application.getStatus() != ApplicationStatus.DOCUMENT_PENDING &&
                application.getStatus() != ApplicationStatus.DOCUMENT_SUBMITTED &&
                application.getStatus() != ApplicationStatus.UNDER_REVIEW) {
            throw new BusinessException(ErrorCode.INVALID_APPLICATION_STATUS,
                    "승인할 수 없는 상태입니다: " + application.getStatus());
        }

        // 신청서 승인 / Approve application
        application.approve(adminId);

        // 도메인 이벤트 발행 / Publish domain event
        eventPublisher.publishEvent(MembershipApplicationApprovedEvent.of(
                application.getId(),
                application.getUserId(),
                application.getApplicantName(),
                adminId
        ));

        log.info("정회원 신청 승인 완료: applicationId={}", applicationId);

        return MembershipApplicationResponse.from(application);
    }

    /**
     * 신청서 반려 / Reject application
     *
     * @param applicationId 신청서 ID
     * @param reason 반려 사유
     * @param adminId 반려하는 관리자 ID
     * @return 반려된 신청서 응답 DTO
     * @throws BusinessException 신청서를 찾을 수 없거나 이미 처리된 경우
     */
    @Transactional
    public MembershipApplicationResponse rejectApplication(Long applicationId, String reason, Long adminId) {
        log.info("정회원 신청 반려 시작: applicationId={}, adminId={}, reason={}", applicationId, adminId, reason);

        MembershipApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

        // 반려 가능 상태 확인 / Check if rejectable
        if (application.getStatus() == ApplicationStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.INVALID_APPLICATION_STATUS,
                    "이미 완료된 신청서는 반려할 수 없습니다");
        }

        // 신청서 반려 / Reject application
        application.reject(reason, adminId);

        log.info("정회원 신청 반려 완료: applicationId={}", applicationId);

        return MembershipApplicationResponse.from(application);
    }

    /**
     * 신청 번호 생성 / Generate application number
     *
     * 형식: APP-YYYYMMDD-XXXXX (예: APP-20251230-00001)
     *
     * @return 생성된 신청 번호
     */
    private String generateApplicationNumber() {
        LocalDateTime now = LocalDateTime.now();
        String datePrefix = String.format("APP-%04d%02d%02d",
                now.getYear(), now.getMonthValue(), now.getDayOfMonth());

        // 오늘 날짜의 마지막 신청 번호 조회
        String lastNumber = applicationRepository.findLatestApplicationNumberByDatePrefix(datePrefix)
                .orElse(datePrefix + "-00000");

        // 순번 증가
        int sequence = Integer.parseInt(lastNumber.substring(lastNumber.length() - 5)) + 1;

        return String.format("%s-%05d", datePrefix, sequence);
    }
}
