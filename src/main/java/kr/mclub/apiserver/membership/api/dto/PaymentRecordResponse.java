package kr.mclub.apiserver.membership.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import kr.mclub.apiserver.membership.domain.PaymentRecord;
import kr.mclub.apiserver.membership.domain.PaymentStatus;
import kr.mclub.apiserver.membership.domain.PaymentType;

/**
 * 결제 기록 응답 DTO
 * Payment record response DTO
 */
public record PaymentRecordResponse(
        Long id,
        Long userId,
        Long applicationId,
        PaymentType paymentType,
        Integer targetYear,
        BigDecimal amount,
        String depositorName,
        LocalDate depositDate,
        PaymentStatus status,
        Long confirmedBy,
        LocalDateTime confirmedAt,
        boolean autoConfirmed,
        String bankTransactionId,
        String bankAccountNumber,
        LocalDateTime cancelledAt,
        Long cancelledBy,
        String cancellationReason,
        LocalDateTime refundedAt,
        BigDecimal refundAmount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * 엔티티로부터 응답 DTO 생성
     * Create response DTO from entity
     */
    public static PaymentRecordResponse from(PaymentRecord payment) {
        return new PaymentRecordResponse(
                payment.getId(),
                payment.getUserId(),
                payment.getApplicationId(),
                payment.getPaymentType(),
                payment.getTargetYear(),
                payment.getAmount(),
                payment.getDepositorName(),
                payment.getDepositDate(),
                payment.getStatus(),
                payment.getConfirmedBy(),
                payment.getConfirmedAt(),
                payment.isAutoConfirmed(),
                payment.getBankTransactionId(),
                payment.getBankAccountNumber(),
                payment.getCancelledAt(),
                payment.getCancelledBy(),
                payment.getCancellationReason(),
                payment.getRefundedAt(),
                payment.getRefundAmount(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
