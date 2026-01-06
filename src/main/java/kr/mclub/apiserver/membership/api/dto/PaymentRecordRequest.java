package kr.mclub.apiserver.membership.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import kr.mclub.apiserver.membership.domain.PaymentType;

/**
 * 결제 기록 요청 DTO
 * Payment record request DTO
 */
public record PaymentRecordRequest(
        Long applicationId,  // 신청서 ID (가입비인 경우 필수, 연회비는 null)
        PaymentType paymentType,  // 결제 유형
        Integer targetYear,  // 대상 년도
        BigDecimal amount,  // 금액
        String depositorName,  // 입금자명
        LocalDate depositDate  // 입금일
) {
}
