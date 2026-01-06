package kr.mclub.apiserver.shared.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 에러 코드 정의
 * Error code definitions for the application
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common Errors (공통 에러)
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C002", "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "서버 내부 오류가 발생했습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C004", "허용되지 않는 HTTP 메서드입니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C005", "잘못된 타입의 값입니다."),

    // Auth Errors (인증 에러)
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "A002", "접근 권한이 없습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A003", "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A004", "유효하지 않은 토큰입니다."),
    OAUTH_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "A005", "OAuth 인증에 실패했습니다."),
    PASSKEY_VERIFICATION_FAILED(HttpStatus.UNAUTHORIZED, "A006", "Passkey 인증에 실패했습니다."),
    CANNOT_DELETE_LAST_CREDENTIAL(HttpStatus.BAD_REQUEST, "A007", "최소 하나의 인증 수단이 필요합니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A008", "이메일 또는 비밀번호가 올바르지 않습니다."),
    PASSWORD_NOT_SET(HttpStatus.BAD_REQUEST, "A009", "비밀번호가 설정되지 않았습니다."),
    USER_WITHDRAWN(HttpStatus.FORBIDDEN, "A010", "탈퇴한 사용자입니다."),
    USER_INACTIVE(HttpStatus.FORBIDDEN, "A011", "비활성화된 계정입니다."),
    OAUTH_TOKEN_EXCHANGE_FAILED(HttpStatus.BAD_REQUEST, "A012", "OAuth 토큰 교환에 실패했습니다."),
    OAUTH_USER_INFO_FAILED(HttpStatus.BAD_REQUEST, "A013", "OAuth 사용자 정보 조회에 실패했습니다."),
    OAUTH_CONFIG_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "A014", "OAuth 설정을 찾을 수 없습니다."),
    OAUTH_PROVIDER_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "A015", "지원하지 않는 OAuth 제공자입니다."),
    NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, "A016", "아직 구현되지 않은 기능입니다."),

    // User Errors (사용자 에러)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U002", "이미 사용 중인 이메일입니다."),
    DUPLICATE_PHONE(HttpStatus.CONFLICT, "U003", "이미 사용 중인 전화번호입니다."),
    INVALID_USER_STATUS(HttpStatus.BAD_REQUEST, "U004", "유효하지 않은 사용자 상태입니다."),
    USER_ALREADY_WITHDRAWN(HttpStatus.BAD_REQUEST, "U005", "이미 탈퇴한 사용자입니다."),

    // Grade Errors (등급 에러)
    GRADE_NOT_FOUND(HttpStatus.NOT_FOUND, "G001", "등급을 찾을 수 없습니다."),
    SYSTEM_GRADE_DELETE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "G002", "시스템 등급은 삭제할 수 없습니다."),
    GRADE_IN_USE(HttpStatus.BAD_REQUEST, "G003", "사용 중인 등급은 삭제할 수 없습니다."),
    DUPLICATE_GRADE_CODE(HttpStatus.CONFLICT, "G004", "이미 존재하는 등급 코드입니다."),

    // Membership Errors (정회원 가입 에러)
    APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "정회원 신청서를 찾을 수 없습니다."),
    APPLICATION_ALREADY_EXISTS(HttpStatus.CONFLICT, "M002", "이미 진행 중인 신청이 있습니다."),
    DOCUMENT_REQUIRED(HttpStatus.BAD_REQUEST, "M003", "필수 서류가 누락되었습니다."),
    DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "M004", "서류를 찾을 수 없습니다."),
    INVALID_APPLICATION_STATUS(HttpStatus.BAD_REQUEST, "M005", "유효하지 않은 신청 상태입니다."),
    OCR_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "M006", "서류 검증에 실패했습니다."),
    DUPLICATE_VIN_NUMBER(HttpStatus.CONFLICT, "M007", "이미 등록된 차대번호입니다."),
    DOCUMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "M008", "이미 등록된 서류입니다."),
    DOCUMENT_ALREADY_VERIFIED(HttpStatus.BAD_REQUEST, "M009", "이미 검증된 서류입니다."),
    OCR_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "M010", "OCR 결과를 찾을 수 없습니다."),
    OCR_SERVICE_NOT_AVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "M011", "OCR 서비스를 사용할 수 없습니다."),
    OCR_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "M012", "OCR이 지원되지 않는 서류 유형입니다."),
    ANNUAL_FEE_CONFIG_NOT_FOUND(HttpStatus.NOT_FOUND, "M013", "연회비 설정을 찾을 수 없습니다."),
    ANNUAL_FEE_CONFIG_ALREADY_EXISTS(HttpStatus.CONFLICT, "M014", "연회비 설정이 이미 존재합니다."),
    MEMBERSHIP_PERIOD_NOT_FOUND(HttpStatus.NOT_FOUND, "M015", "멤버십 기간을 찾을 수 없습니다."),
    MEMBERSHIP_ALREADY_RENEWED(HttpStatus.CONFLICT, "M016", "이미 갱신된 멤버십입니다."),

    // Payment Errors (결제 에러)
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "결제 정보를 찾을 수 없습니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "P002", "결제 금액이 일치하지 않습니다."),
    PAYMENT_ALREADY_CONFIRMED(HttpStatus.CONFLICT, "P003", "이미 확인된 결제입니다."),
    MEMBERSHIP_EXPIRED(HttpStatus.PAYMENT_REQUIRED, "P004", "멤버십이 만료되었습니다."),
    INVALID_PAYMENT_STATUS(HttpStatus.BAD_REQUEST, "P005", "유효하지 않은 결제 상태입니다."),
    INVALID_REFUND_AMOUNT(HttpStatus.BAD_REQUEST, "P006", "유효하지 않은 환불 금액입니다."),

    // Vehicle Errors (차량 에러)
    VEHICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "V001", "차량을 찾을 수 없습니다."),
    NO_ACTIVE_VEHICLE(HttpStatus.BAD_REQUEST, "V002", "활성화된 차량이 없습니다."),
    VEHICLE_GRACE_PERIOD_EXPIRED(HttpStatus.BAD_REQUEST, "V003", "차량 유예 기간이 만료되었습니다."),

    // Community Errors (커뮤니티 에러)
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "B001", "게시판을 찾을 수 없습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "B002", "게시글을 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "B003", "댓글을 찾을 수 없습니다."),
    BOARD_ACCESS_DENIED(HttpStatus.FORBIDDEN, "B004", "게시판 접근 권한이 없습니다."),
    NOT_POST_AUTHOR(HttpStatus.FORBIDDEN, "B005", "게시글 작성자가 아닙니다."),
    NOT_COMMENT_AUTHOR(HttpStatus.FORBIDDEN, "B006", "댓글 작성자가 아닙니다."),

    // Permission Group Errors (권한 그룹 에러)
    PERMISSION_GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "PG001", "권한 그룹을 찾을 수 없습니다."),
    DEFAULT_GROUP_DELETE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "PG002", "기본 권한 그룹은 삭제할 수 없습니다."),
    DUPLICATE_PERMISSION_GROUP_NAME(HttpStatus.CONFLICT, "PG003", "이미 존재하는 권한 그룹 이름입니다."),

    // Event Errors (이벤트 에러)
    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "E001", "이벤트를 찾을 수 없습니다."),
    EVENT_FULL(HttpStatus.BAD_REQUEST, "E002", "이벤트 정원이 초과되었습니다."),
    EVENT_ALREADY_PARTICIPATED(HttpStatus.CONFLICT, "E003", "이미 참가 신청한 이벤트입니다."),
    EVENT_NOT_PARTICIPATED(HttpStatus.BAD_REQUEST, "E004", "참가 신청하지 않은 이벤트입니다."),

    // Chat Errors (채팅 에러)
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CH001", "채팅방을 찾을 수 없습니다."),
    NOT_ROOM_PARTICIPANT(HttpStatus.FORBIDDEN, "CH002", "채팅방 참여자가 아닙니다."),

    // File Errors (파일 에러)
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F001", "파일 업로드에 실패했습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "F002", "파일을 찾을 수 없습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "F003", "허용되지 않는 파일 형식입니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "F004", "파일 크기가 초과되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
