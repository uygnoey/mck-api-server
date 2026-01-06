package kr.mclub.apiserver.membership.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import kr.mclub.apiserver.membership.domain.PaymentType;

/**
 * 결제 확인 이벤트 / Payment Confirmed Event
 *
 * @param paymentId 결제 기록 ID
 * @param userId 사용자 ID
 * @param paymentType 결제 유형 (가입비/연회비)
 * @param amount 금액
 * @param targetYear 대상 년도
 * @param depositDate 입금일
 * @param confirmedBy 확인한 관리자 ID (null이면 자동 확인)
 * @param confirmedAt 확인 시각
 * @since 1.0
 */
public record PaymentConfirmedEvent(
        Long paymentId,
        Long userId,
        PaymentType paymentType,
        BigDecimal amount,
        Integer targetYear,
        LocalDate depositDate,
        Long confirmedBy,
        LocalDateTime confirmedAt
) {
    public static PaymentConfirmedEvent of(
            Long paymentId,
            Long userId,
            PaymentType paymentType,
            BigDecimal amount,
            Integer targetYear,
            LocalDate depositDate,
            Long confirmedBy
    ) {
        return new PaymentConfirmedEvent(
                paymentId,
                userId,
                paymentType,
                amount,
                targetYear,
                depositDate,
                confirmedBy,
                LocalDateTime.now()
        );
    }
}
