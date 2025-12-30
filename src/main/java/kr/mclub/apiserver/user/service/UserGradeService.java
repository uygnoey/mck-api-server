package kr.mclub.apiserver.user.service;

import kr.mclub.apiserver.shared.exception.BusinessException;
import kr.mclub.apiserver.shared.exception.ErrorCode;
import kr.mclub.apiserver.user.domain.UserGrade;
import kr.mclub.apiserver.user.repository.UserGradeRepository;
import kr.mclub.apiserver.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 사용자 등급 서비스
 * User grade service
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserGradeService {

    private final UserGradeRepository userGradeRepository;
    private final UserRepository userRepository;

    /**
     * 모든 활성 등급 조회
     * Get all active grades
     */
    public List<UserGrade> getAllActiveGrades() {
        return userGradeRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    /**
     * 등급 코드로 조회
     * Get grade by code
     */
    public UserGrade getGradeByCode(String code) {
        return userGradeRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(ErrorCode.GRADE_NOT_FOUND, code));
    }

    /**
     * 등급 ID로 조회
     * Get grade by ID
     */
    public UserGrade getGradeById(Long id) {
        return userGradeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.GRADE_NOT_FOUND));
    }

    /**
     * 신규 사용자 기본 등급 (준회원)
     * Get default grade for new user (Associate)
     */
    public UserGrade getDefaultGradeForNewUser() {
        return userGradeRepository.findByCode("ASSOCIATE")
                .orElseThrow(() -> new BusinessException(ErrorCode.GRADE_NOT_FOUND, "ASSOCIATE"));
    }

    /**
     * 등급 생성
     * Create new grade
     */
    @Transactional
    public UserGrade createGrade(String code, String name, String roleName,
                                 Integer permissionLevel, boolean isExecutive, boolean isStaff,
                                 boolean isMember, boolean requiresAnnualFee,
                                 String displaySuffix, Integer displayOrder, Long creatorId) {

        // 코드 중복 체크
        if (userGradeRepository.existsByCode(code)) {
            throw new BusinessException(ErrorCode.DUPLICATE_GRADE_CODE, code);
        }

        // Role 이름 중복 체크
        String roleNameToUse = roleName != null ? roleName : "ROLE_" + code;
        if (userGradeRepository.existsByRoleName(roleNameToUse)) {
            throw new BusinessException(ErrorCode.DUPLICATE_GRADE_CODE, roleNameToUse);
        }

        UserGrade grade = UserGrade.builder()
                .code(code)
                .name(name)
                .roleName(roleNameToUse)
                .permissionLevel(permissionLevel)
                .isExecutive(isExecutive)
                .isStaff(isStaff)
                .isMember(isMember)
                .requiresAnnualFee(requiresAnnualFee)
                .isSystemGrade(false)  // 사용자가 만든 등급은 시스템 등급이 아님
                .displaySuffix(displaySuffix)
                .displayOrder(displayOrder)
                .createdBy(creatorId)
                .build();

        return userGradeRepository.save(grade);
    }

    /**
     * 등급 수정
     * Update grade
     */
    @Transactional
    public UserGrade updateGrade(Long gradeId, String name, Integer permissionLevel,
                                 boolean isExecutive, boolean isStaff,
                                 String displaySuffix, Integer displayOrder) {

        UserGrade grade = getGradeById(gradeId);
        grade.update(name, permissionLevel, isExecutive, isStaff, displaySuffix, displayOrder);
        return grade;
    }

    /**
     * 등급 삭제
     * Delete grade (soft delete by deactivation)
     */
    @Transactional
    public void deleteGrade(Long gradeId) {
        UserGrade grade = getGradeById(gradeId);

        // 시스템 등급은 삭제 불가
        if (grade.isSystemGrade()) {
            throw new BusinessException(ErrorCode.SYSTEM_GRADE_DELETE_NOT_ALLOWED, grade.getCode());
        }

        // 사용 중인 등급은 삭제 불가
        if (userRepository.existsByGradeId(gradeId)) {
            throw new BusinessException(ErrorCode.GRADE_IN_USE, grade.getCode());
        }

        grade.deactivate();
    }

    /**
     * 삭제 가능한 등급 목록
     * Get deletable grades
     */
    public List<UserGrade> getDeletableGrades() {
        return userGradeRepository.findDeletableGrades();
    }

    /**
     * 임원 등급 목록
     * Get executive grades
     */
    public List<UserGrade> getExecutiveGrades() {
        return userGradeRepository.findByIsExecutiveTrueAndIsActiveTrue();
    }

    /**
     * 운영진 등급 목록
     * Get staff grades
     */
    public List<UserGrade> getStaffGrades() {
        return userGradeRepository.findByIsStaffTrueAndIsActiveTrue();
    }
}
