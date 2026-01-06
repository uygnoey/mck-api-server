package kr.mclub.apiserver.user.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import kr.mclub.apiserver.shared.exception.BusinessException;
import kr.mclub.apiserver.shared.exception.ErrorCode;
import kr.mclub.apiserver.user.api.dto.UserGradeCreateRequest;
import kr.mclub.apiserver.user.api.dto.UserGradeUpdateRequest;
import kr.mclub.apiserver.user.domain.UserGrade;
import kr.mclub.apiserver.user.repository.UserGradeRepository;
import kr.mclub.apiserver.user.repository.UserRepository;

/**
 * 사용자 등급 서비스
 * User grade service
 */
@Slf4j
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
     *
     * @param request 등급 생성 요청 DTO
     * @param creatorId 생성자 ID
     * @return 생성된 등급
     */
    @Transactional
    public UserGrade createGrade(UserGradeCreateRequest request, Long creatorId) {

        // 코드 중복 체크
        if (userGradeRepository.existsByCode(request.code())) {
            throw new BusinessException(ErrorCode.DUPLICATE_GRADE_CODE, request.code());
        }

        // Role 이름 중복 체크
        String roleNameToUse = request.roleName() != null ? request.roleName() : "ROLE_" + request.code();
        if (userGradeRepository.existsByRoleName(roleNameToUse)) {
            throw new BusinessException(ErrorCode.DUPLICATE_GRADE_CODE, roleNameToUse);
        }

        UserGrade grade = UserGrade.builder()
                .code(request.code())
                .name(request.name())
                .roleName(roleNameToUse)
                .permissionLevel(request.permissionLevel())
                .isExecutive(request.isExecutive())
                .isStaff(request.isStaff())
                .isMember(request.isMember())
                .requiresAnnualFee(request.requiresAnnualFee())
                .isSystemGrade(false)  // 사용자가 만든 등급은 시스템 등급이 아님
                .displaySuffix(request.displaySuffix())
                .displayOrder(request.displayOrder())
                .createdBy(creatorId)
                .build();

        UserGrade savedGrade = userGradeRepository.save(grade);
        log.info("New user grade created: code={}, name={}, createdBy={}",
                request.code(), request.name(), creatorId);

        return savedGrade;
    }

    /**
     * 등급 수정
     * Update grade
     *
     * @param gradeId 등급 ID
     * @param request 등급 수정 요청 DTO
     * @return 수정된 등급
     */
    @Transactional
    public UserGrade updateGrade(Long gradeId, UserGradeUpdateRequest request) {

        UserGrade grade = getGradeById(gradeId);
        grade.update(
                request.name(),
                request.permissionLevel(),
                request.isExecutive(),
                request.isStaff(),
                request.displaySuffix(),
                request.displayOrder()
        );

        log.info("User grade updated: gradeId={}, name={}", gradeId, request.name());
        return grade;
    }

    /**
     * 등급 삭제
     * Delete grade (soft delete by deactivation)
     *
     * @param gradeId 등급 ID
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
        log.info("User grade deleted (deactivated): gradeId={}, code={}", gradeId, grade.getCode());
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
