package kr.mclub.apiserver.membership.api;

import java.math.BigDecimal;
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

import kr.mclub.apiserver.membership.api.dto.PaymentRecordRequest;
import kr.mclub.apiserver.membership.api.dto.PaymentRecordResponse;
import kr.mclub.apiserver.membership.domain.PaymentStatus;
import kr.mclub.apiserver.membership.service.PaymentService;
import kr.mclub.apiserver.shared.security.CurrentUser;
import kr.mclub.apiserver.shared.util.ApiResponse;

/**
 * 결제 Controller / Payment Controller
 *
 * <p>정회원 가입비 및 연회비 결제를 관리하는 REST API를 제공합니다.</p>
 * <p>Provides REST API for managing membership enrollment fee and annual fee payments.</p>
 *
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/membership/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 기록 등록 / Register payment record
     *
     * @param userId 사용자 ID (인증된 사용자)
     * @param request 결제 기록 요청 DTO
     * @return 등록된 결제 기록 응답 DTO
     */
    @PostMapping
    public ApiResponse<PaymentRecordResponse> registerPayment(
            @CurrentUser Long userId,
            @RequestBody PaymentRecordRequest request) {
        log.info("결제 기록 등록 요청: userId={}, paymentType={}, amount={}",
                userId, request.paymentType(), request.amount());

        PaymentRecordResponse response = paymentService.registerPayment(userId, request);

        return ApiResponse.success(response);
    }

    /**
     * 내 결제 기록 목록 조회 / Get my payment records
     *
     * @param userId 사용자 ID (인증된 사용자)
     * @return 결제 기록 목록
     */
    @GetMapping("/me")
    public ApiResponse<List<PaymentRecordResponse>> getMyPayments(@CurrentUser Long userId) {
        log.info("내 결제 기록 조회: userId={}", userId);

        List<PaymentRecordResponse> responses = paymentService.getPaymentsByUserId(userId);

        return ApiResponse.success(responses);
    }

    /**
     * 결제 ID로 조회 / Get payment by ID
     *
     * @param paymentId 결제 ID
     * @return 결제 기록 응답 DTO
     */
    @GetMapping("/{paymentId}")
    public ApiResponse<PaymentRecordResponse> getPaymentById(@PathVariable Long paymentId) {
        log.info("결제 기록 조회: paymentId={}", paymentId);

        PaymentRecordResponse response = paymentService.getPaymentById(paymentId);

        return ApiResponse.success(response);
    }

    /**
     * 신청서의 결제 기록 조회 (관리자) / Get payments by application ID (admin)
     *
     * @param applicationId 신청서 ID
     * @return 결제 기록 목록
     */
    @GetMapping("/application/{applicationId}")
    public ApiResponse<List<PaymentRecordResponse>> getPaymentsByApplicationId(@PathVariable Long applicationId) {
        log.info("신청서 결제 기록 조회: applicationId={}", applicationId);

        List<PaymentRecordResponse> responses = paymentService.getPaymentsByApplicationId(applicationId);

        return ApiResponse.success(responses);
    }

    /**
     * 상태별 결제 목록 조회 (관리자) / Get payments by status (admin)
     *
     * @param status 결제 상태
     * @return 결제 기록 목록
     */
    @GetMapping
    public ApiResponse<List<PaymentRecordResponse>> getPaymentsByStatus(
            @RequestParam(required = false) PaymentStatus status) {
        log.info("상태별 결제 목록 조회: status={}", status);

        List<PaymentRecordResponse> responses;
        if (status != null) {
            responses = paymentService.getPaymentsByStatus(status);
        } else {
            responses = List.of();
        }

        return ApiResponse.success(responses);
    }

    /**
     * 대기 중인 결제 목록 조회 (관리자) / Get pending payments (admin)
     *
     * @return 대기 중인 결제 기록 목록
     */
    @GetMapping("/pending")
    public ApiResponse<List<PaymentRecordResponse>> getPendingPayments() {
        log.info("대기 중인 결제 목록 조회");

        List<PaymentRecordResponse> responses = paymentService.getPendingPayments();

        return ApiResponse.success(responses);
    }

    /**
     * 결제 확인 (관리자) / Confirm payment (admin)
     *
     * @param paymentId 결제 ID
     * @param adminId 관리자 ID (인증된 관리자)
     * @return 확인된 결제 기록 응답 DTO
     */
    @PostMapping("/{paymentId}/confirm")
    public ApiResponse<PaymentRecordResponse> confirmPayment(
            @PathVariable Long paymentId,
            @CurrentUser Long adminId) {
        log.info("결제 확인 요청: paymentId={}, adminId={}", paymentId, adminId);

        PaymentRecordResponse response = paymentService.confirmPayment(paymentId, adminId);

        return ApiResponse.success(response);
    }

    /**
     * 결제 취소 (관리자) / Cancel payment (admin)
     *
     * @param paymentId 결제 ID
     * @param adminId 관리자 ID (인증된 관리자)
     * @param reason 취소 사유
     * @return 취소된 결제 기록 응답 DTO
     */
    @PostMapping("/{paymentId}/cancel")
    public ApiResponse<PaymentRecordResponse> cancelPayment(
            @PathVariable Long paymentId,
            @CurrentUser Long adminId,
            @RequestParam String reason) {
        log.info("결제 취소 요청: paymentId={}, adminId={}, reason={}", paymentId, adminId, reason);

        PaymentRecordResponse response = paymentService.cancelPayment(paymentId, reason, adminId);

        return ApiResponse.success(response);
    }

    /**
     * 결제 환불 (관리자) / Refund payment (admin)
     *
     * @param paymentId 결제 ID
     * @param adminId 관리자 ID (인증된 관리자)
     * @param refundAmount 환불 금액
     * @return 환불된 결제 기록 응답 DTO
     */
    @PostMapping("/{paymentId}/refund")
    public ApiResponse<PaymentRecordResponse> refundPayment(
            @PathVariable Long paymentId,
            @CurrentUser Long adminId,
            @RequestParam BigDecimal refundAmount) {
        log.info("결제 환불 요청: paymentId={}, adminId={}, refundAmount={}", paymentId, adminId, refundAmount);

        PaymentRecordResponse response = paymentService.refundPayment(paymentId, refundAmount, adminId);

        return ApiResponse.success(response);
    }

    /**
     * 특정 년도 연회비 납부 여부 확인 / Check if annual fee is paid for year
     *
     * @param userId 사용자 ID (인증된 사용자)
     * @param year 년도
     * @return 납부 여부
     */
    @GetMapping("/annual-fee/check")
    public ApiResponse<Boolean> checkAnnualFeePaid(
            @CurrentUser Long userId,
            @RequestParam Integer year) {
        log.info("연회비 납부 여부 확인: userId={}, year={}", userId, year);

        boolean isPaid = paymentService.isAnnualFeePaid(userId, year);

        return ApiResponse.success(isPaid);
    }
}
