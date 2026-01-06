package kr.mclub.apiserver.membership.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import kr.mclub.apiserver.membership.api.dto.PaymentRecordRequest;
import kr.mclub.apiserver.membership.api.dto.PaymentRecordResponse;
import kr.mclub.apiserver.membership.domain.MembershipApplication;
import kr.mclub.apiserver.membership.domain.PaymentRecord;
import kr.mclub.apiserver.membership.domain.PaymentStatus;
import kr.mclub.apiserver.membership.domain.PaymentType;
import kr.mclub.apiserver.membership.event.PaymentConfirmedEvent;
import kr.mclub.apiserver.membership.repository.MembershipApplicationRepository;
import kr.mclub.apiserver.membership.repository.PaymentRecordRepository;
import kr.mclub.apiserver.shared.exception.BusinessException;
import kr.mclub.apiserver.shared.exception.ErrorCode;

/**
 * 결제 Service / Payment Service
 *
 * <p>정회원 가입비 및 연회비 결제를 관리합니다.</p>
 * <p>Manages membership enrollment fee and annual fee payments.</p>
 *
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRecordRepository paymentRepository;
    private final MembershipApplicationRepository applicationRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 결제 기록 등록 / Register payment record
     *
     * @param userId 사용자 ID
     * @param request 결제 기록 요청
     * @return 등록된 결제 기록 응답 DTO
     * @throws BusinessException 신청서를 찾을 수 없거나 중복 결제인 경우
     */
    @Transactional
    public PaymentRecordResponse registerPayment(Long userId, PaymentRecordRequest request) {
        log.info("결제 기록 등록 시작: userId={}, paymentType={}, amount={}",
                userId, request.paymentType(), request.amount());

        // 가입비인 경우 신청서 확인
        if (request.paymentType() == PaymentType.ENROLLMENT_FEE && request.applicationId() != null) {
            MembershipApplication application = applicationRepository.findById(request.applicationId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

            // 이미 결제 확인된 가입비가 있는지 확인
            paymentRepository.findByApplicationIdAndPaymentTypeAndStatus(
                            request.applicationId(), PaymentType.ENROLLMENT_FEE, PaymentStatus.CONFIRMED)
                    .ifPresent(existing -> {
                        throw new BusinessException(ErrorCode.PAYMENT_ALREADY_CONFIRMED,
                                "이미 확인된 가입비 결제가 있습니다");
                    });
        }

        // 연회비인 경우 해당 년도 중복 확인
        if (request.paymentType() == PaymentType.ANNUAL_FEE) {
            paymentRepository.findByUserIdAndPaymentTypeAndTargetYearAndStatus(
                            userId, PaymentType.ANNUAL_FEE, request.targetYear(), PaymentStatus.CONFIRMED)
                    .ifPresent(existing -> {
                        throw new BusinessException(ErrorCode.PAYMENT_ALREADY_CONFIRMED,
                                String.format("%d년도 연회비가 이미 납부되었습니다", request.targetYear()));
                    });
        }

        // 결제 기록 생성
        PaymentRecord payment = PaymentRecord.builder()
                .userId(userId)
                .applicationId(request.applicationId())
                .paymentType(request.paymentType())
                .targetYear(request.targetYear())
                .amount(request.amount())
                .depositorName(request.depositorName())
                .depositDate(request.depositDate())
                .build();

        PaymentRecord savedPayment = paymentRepository.save(payment);

        log.info("결제 기록 등록 완료: paymentId={}", savedPayment.getId());
        return PaymentRecordResponse.from(savedPayment);
    }

    /**
     * 결제 확인 (관리자) / Confirm payment by admin
     *
     * @param paymentId 결제 ID
     * @param adminId 관리자 ID
     * @return 확인된 결제 기록 응답 DTO
     * @throws BusinessException 결제를 찾을 수 없거나 이미 확인된 경우
     */
    @Transactional
    public PaymentRecordResponse confirmPayment(Long paymentId, Long adminId) {
        log.info("결제 확인 시작: paymentId={}, adminId={}", paymentId, adminId);

        PaymentRecord payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        // 이미 확인된 결제인지 확인
        if (payment.getStatus() == PaymentStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_CONFIRMED,
                    "이미 확인된 결제입니다");
        }

        // 취소된 결제는 확인 불가
        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS,
                    "취소된 결제는 확인할 수 없습니다");
        }

        // 결제 확인
        payment.confirm(adminId);

        // 결제 확인 이벤트 발행
        eventPublisher.publishEvent(PaymentConfirmedEvent.of(
                payment.getId(),
                payment.getUserId(),
                payment.getPaymentType(),
                payment.getAmount(),
                payment.getTargetYear(),
                payment.getDepositDate(),
                adminId
        ));

        // 가입비인 경우 신청서 상태 업데이트
        if (payment.getPaymentType() == PaymentType.ENROLLMENT_FEE && payment.getApplicationId() != null) {
            MembershipApplication application = applicationRepository.findById(payment.getApplicationId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

            application.confirmPayment();
            log.info("신청서 결제 확인 완료: applicationId={}", application.getId());
        }

        log.info("결제 확인 완료: paymentId={}", paymentId);
        return PaymentRecordResponse.from(payment);
    }

    /**
     * 결제 자동 확인 (오픈뱅킹) / Auto-confirm payment via open banking
     *
     * @param paymentId 결제 ID
     * @param bankTransactionId 은행 거래 ID
     * @param bankAccountNumber 입금 계좌번호
     * @return 확인된 결제 기록 응답 DTO
     * @throws BusinessException 결제를 찾을 수 없거나 이미 확인된 경우
     */
    @Transactional
    public PaymentRecordResponse autoConfirmPayment(Long paymentId, String bankTransactionId, String bankAccountNumber) {
        log.info("결제 자동 확인 시작: paymentId={}, bankTransactionId={}", paymentId, bankTransactionId);

        PaymentRecord payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        // 이미 확인된 결제인지 확인
        if (payment.getStatus() == PaymentStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_CONFIRMED,
                    "이미 확인된 결제입니다");
        }

        // 결제 자동 확인
        payment.autoConfirm(bankTransactionId, bankAccountNumber);

        // 결제 확인 이벤트 발행 (자동 확인이므로 adminId는 null)
        eventPublisher.publishEvent(PaymentConfirmedEvent.of(
                payment.getId(),
                payment.getUserId(),
                payment.getPaymentType(),
                payment.getAmount(),
                payment.getTargetYear(),
                payment.getDepositDate(),
                null
        ));

        // 가입비인 경우 신청서 상태 업데이트
        if (payment.getPaymentType() == PaymentType.ENROLLMENT_FEE && payment.getApplicationId() != null) {
            MembershipApplication application = applicationRepository.findById(payment.getApplicationId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

            application.confirmPayment();
            log.info("신청서 결제 자동 확인 완료: applicationId={}", application.getId());
        }

        log.info("결제 자동 확인 완료: paymentId={}", paymentId);
        return PaymentRecordResponse.from(payment);
    }

    /**
     * 결제 취소 / Cancel payment
     *
     * @param paymentId 결제 ID
     * @param reason 취소 사유
     * @param adminId 관리자 ID
     * @return 취소된 결제 기록 응답 DTO
     * @throws BusinessException 결제를 찾을 수 없는 경우
     */
    @Transactional
    public PaymentRecordResponse cancelPayment(Long paymentId, String reason, Long adminId) {
        log.info("결제 취소 시작: paymentId={}, adminId={}, reason={}", paymentId, adminId, reason);

        PaymentRecord payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        // 결제 취소
        payment.cancel(reason, adminId);

        log.info("결제 취소 완료: paymentId={}", paymentId);
        return PaymentRecordResponse.from(payment);
    }

    /**
     * 결제 환불 / Refund payment
     *
     * @param paymentId 결제 ID
     * @param refundAmount 환불 금액
     * @param adminId 관리자 ID
     * @return 환불된 결제 기록 응답 DTO
     * @throws BusinessException 결제를 찾을 수 없거나 확인되지 않은 경우
     */
    @Transactional
    public PaymentRecordResponse refundPayment(Long paymentId, BigDecimal refundAmount, Long adminId) {
        log.info("결제 환불 시작: paymentId={}, refundAmount={}, adminId={}", paymentId, refundAmount, adminId);

        PaymentRecord payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        // 확인된 결제만 환불 가능
        if (payment.getStatus() != PaymentStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS,
                    "확인된 결제만 환불할 수 있습니다");
        }

        // 환불 금액이 결제 금액보다 큰 경우
        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_AMOUNT,
                    "환불 금액이 결제 금액보다 클 수 없습니다");
        }

        // 환불 처리
        payment.refund(refundAmount);

        log.info("결제 환불 완료: paymentId={}", paymentId);
        return PaymentRecordResponse.from(payment);
    }

    /**
     * 사용자의 결제 기록 조회 / Get payment records by user ID
     *
     * @param userId 사용자 ID
     * @return 결제 기록 목록
     */
    public List<PaymentRecordResponse> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(PaymentRecordResponse::from)
                .toList();
    }

    /**
     * 신청서의 결제 기록 조회 / Get payment records by application ID
     *
     * @param applicationId 신청서 ID
     * @return 결제 기록 목록
     */
    public List<PaymentRecordResponse> getPaymentsByApplicationId(Long applicationId) {
        return paymentRepository.findByApplicationId(applicationId).stream()
                .map(PaymentRecordResponse::from)
                .toList();
    }

    /**
     * 결제 ID로 조회 / Get payment by ID
     *
     * @param paymentId 결제 ID
     * @return 결제 기록 응답 DTO
     * @throws BusinessException 결제를 찾을 수 없는 경우
     */
    public PaymentRecordResponse getPaymentById(Long paymentId) {
        PaymentRecord payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        return PaymentRecordResponse.from(payment);
    }

    /**
     * 상태별 결제 목록 조회 / Get payments by status
     *
     * @param status 결제 상태
     * @return 결제 기록 목록
     */
    public List<PaymentRecordResponse> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(PaymentRecordResponse::from)
                .toList();
    }

    /**
     * 대기 중인 결제 목록 조회 / Get pending payments
     *
     * @return 대기 중인 결제 기록 목록
     */
    public List<PaymentRecordResponse> getPendingPayments() {
        return getPaymentsByStatus(PaymentStatus.PENDING);
    }

    /**
     * 특정 년도 연회비 납부 여부 확인 / Check if annual fee is paid for year
     *
     * @param userId 사용자 ID
     * @param year 년도
     * @return 납부 여부
     */
    public boolean isAnnualFeePaid(Long userId, Integer year) {
        return paymentRepository.findByUserIdAndPaymentTypeAndTargetYearAndStatus(
                userId, PaymentType.ANNUAL_FEE, year, PaymentStatus.CONFIRMED
        ).isPresent();
    }
}
