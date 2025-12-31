/**
 * Membership Module - 정회원 가입 모듈
 *
 * <p>정회원 가입 신청, 서류 검증, 결제 관리, 연회비 갱신 기능을 제공합니다.</p>
 *
 * <h2>주요 기능</h2>
 * <ul>
 *   <li>정회원 가입 신청 및 서류 제출</li>
 *   <li>OCR 기반 서류 자동 검증</li>
 *   <li>입금 확인 및 결제 관리</li>
 *   <li>연회비 자동 갱신 및 이월 처리</li>
 *   <li>차량 관리 (등록, 매각, 유예기간)</li>
 *   <li>이사 파트 관리</li>
 * </ul>
 *
 * <h2>Published Events</h2>
 * <ul>
 *   <li>{@code MembershipAppliedEvent} - 정회원 신청 시</li>
 *   <li>{@code MembershipApprovedEvent} - 신청 승인 시</li>
 *   <li>{@code MembershipRejectedEvent} - 신청 반려 시</li>
 *   <li>{@code MembershipExpiredEvent} - 멤버십 만료 시</li>
 *   <li>{@code PaymentConfirmedEvent} - 입금 확인 시</li>
 *   <li>{@code VehicleAddedEvent} - 차량 등록 시</li>
 *   <li>{@code VehicleSoldEvent} - 차량 매각 시</li>
 * </ul>
 *
 * <h2>Subscribed Events</h2>
 * <ul>
 *   <li>{@code UserRegisteredEvent} - 사용자 등록 시 준회원 상태로 시작</li>
 * </ul>
 *
 * @since 1.0
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "Membership Module",
        allowedDependencies = {"shared", "user", "shared :: domain"}
)
package kr.mclub.apiserver.membership;
