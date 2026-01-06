package kr.mclub.apiserver.user.service;

import kr.mclub.apiserver.shared.exception.BusinessException;
import kr.mclub.apiserver.shared.exception.ErrorCode;
import kr.mclub.apiserver.user.domain.ExemptionType;
import kr.mclub.apiserver.user.domain.User;
import kr.mclub.apiserver.user.domain.UserGrade;
import kr.mclub.apiserver.user.event.UserEventPublisher;
import kr.mclub.apiserver.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 사용자 서비스
 * User service
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserGradeService userGradeService;
    private final UserEventPublisher eventPublisher;

    /**
     * ID로 사용자 조회
     * Get user by ID
     */
    public User getUserById(Long id) {
        return userRepository.findByIdAndNotWithdrawn(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 이메일로 사용자 조회
     * Get user by email
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 정회원 번호로 사용자 조회
     * Get user by member number
     */
    public User getUserByMemberNumber(Integer memberNumber) {
        return userRepository.findByMemberNumber(memberNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 프로필 수정
     * Update user profile
     */
    @Transactional
    public User updateProfile(Long userId, String phoneNumber, String profileImageUrl) {
        User user = getUserById(userId);
        user.updateProfile(phoneNumber, profileImageUrl);
        return user;
    }

    /**
     * 등급 변경
     * Change user grade
     */
    @Transactional
    public User changeGrade(Long userId, Long newGradeId, Long adminId) {
        User user = getUserById(userId);
        UserGrade oldGrade = user.getGrade();
        UserGrade newGrade = userGradeService.getGradeById(newGradeId);

        user.changeGrade(newGrade);

        // 이벤트 발행
        eventPublisher.publishGradeChanged(user, oldGrade, adminId);

        return user;
    }

    /**
     * 정회원 번호 부여
     * Assign member number
     */
    @Transactional
    public User assignMemberNumber(Long userId) {
        User user = getUserById(userId);

        if (user.getMemberNumber() != null) {
            throw new BusinessException(ErrorCode.INVALID_USER_STATUS, "이미 정회원 번호가 있습니다.");
        }

        Integer nextNumber = userRepository.getNextMemberNumber();
        user.assignMemberNumber(nextNumber);

        return user;
    }

    /**
     * 연회비 면제 부여
     * Grant annual fee exemption
     */
    @Transactional
    public User grantExemption(Long userId, ExemptionType type, String reason, Integer year) {
        User user = getUserById(userId);
        user.grantExemption(type, reason, year);
        return user;
    }

    /**
     * 연회비 면제 해제
     * Revoke annual fee exemption
     */
    @Transactional
    public User revokeExemption(Long userId) {
        User user = getUserById(userId);
        user.revokeExemption();
        return user;
    }

    /**
     * 회원 탈퇴
     * Withdraw membership
     */
    @Transactional
    public void withdraw(Long userId, String reason) {
        User user = getUserById(userId);

        if (user.isWithdrawn()) {
            throw new BusinessException(ErrorCode.USER_ALREADY_WITHDRAWN);
        }

        user.withdraw(reason);

        // 이벤트 발행
        eventPublisher.publishUserWithdrawn(user);
    }

    /**
     * 마지막 로그인 시간 업데이트
     * Update last login time
     */
    @Transactional
    public void updateLastLoginAt(Long userId) {
        User user = getUserById(userId);
        user.updateLastLoginAt();
    }

    /**
     * 키워드로 사용자 검색
     * Search users by keyword
     */
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        return userRepository.searchByKeyword(keyword, pageable);
    }

    /**
     * 등급별 사용자 조회
     * Get users by grade code
     */
    public Page<User> getUsersByGradeCode(String gradeCode, Pageable pageable) {
        return userRepository.findActiveUsersByGradeCode(gradeCode, pageable);
    }

    /**
     * 연회비 갱신 대상 정회원 조회
     * Get regular members for renewal
     */
    public List<User> getRegularMembersForRenewal() {
        return userRepository.findRegularMembersForRenewal();
    }

    /**
     * 화면 표시용 이름 조회
     * Get display name
     */
    public String getDisplayName(Long userId) {
        User user = getUserById(userId);
        return user.getDisplayName();
    }
}
