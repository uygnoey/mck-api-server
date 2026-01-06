package kr.mclub.apiserver.user.api;

import java.util.List;
import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import kr.mclub.apiserver.shared.security.CurrentUser;
import kr.mclub.apiserver.shared.util.ApiResponse;
import kr.mclub.apiserver.user.api.dto.UserGradeCreateRequest;
import kr.mclub.apiserver.user.api.dto.UserGradeResponse;
import kr.mclub.apiserver.user.api.dto.UserGradeUpdateRequest;
import kr.mclub.apiserver.user.domain.UserGrade;
import kr.mclub.apiserver.user.service.UserGradeService;

/**
 * 관리자 등급 관리 API 컨트롤러
 * Admin user grade management API controller
 *
 * <p>회장(PRESIDENT) 권한이 있는 사용자만 접근 가능</p>
 */
@RestController
@RequestMapping("/api/v1/admin/user-grades")
@RequiredArgsConstructor
public class AdminUserGradeController {

    private final UserGradeService userGradeService;

    /**
     * 등급 목록 조회
     * Get all grades
     *
     * GET /api/v1/admin/user-grades
     */
    @GetMapping
    @PreAuthorize("hasRole('DIRECTOR')")
    public ApiResponse<List<UserGradeResponse>> getAllGrades() {
        List<UserGrade> grades = userGradeService.getAllActiveGrades();
        List<UserGradeResponse> response = grades.stream()
                .map(UserGradeResponse::from)
                .toList();
        return ApiResponse.success(response);
    }

    /**
     * 등급 생성 (회장만 가능)
     * Create new grade (president only)
     *
     * POST /api/v1/admin/user-grades
     */
    @PostMapping
    @PreAuthorize("hasRole('PRESIDENT')")
    public ApiResponse<UserGradeResponse> createGrade(
            @CurrentUser Long adminId,
            @Valid @RequestBody UserGradeCreateRequest request) {

        UserGrade grade = userGradeService.createGrade(request, adminId);
        return ApiResponse.success(UserGradeResponse.from(grade));
    }

    /**
     * 등급 수정
     * Update grade
     *
     * PUT /api/v1/admin/user-grades/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENT')")
    public ApiResponse<UserGradeResponse> updateGrade(
            @PathVariable Long id,
            @Valid @RequestBody UserGradeUpdateRequest request) {

        UserGrade grade = userGradeService.updateGrade(id, request);
        return ApiResponse.success(UserGradeResponse.from(grade));
    }

    /**
     * 등급 삭제 (비활성화)
     * Delete grade (deactivate)
     *
     * DELETE /api/v1/admin/user-grades/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRESIDENT')")
    public ApiResponse<Void> deleteGrade(@PathVariable Long id) {
        userGradeService.deleteGrade(id);
        return ApiResponse.success("등급이 삭제되었습니다.");
    }

    /**
     * 삭제 가능한 등급 목록
     * Get deletable grades
     *
     * GET /api/v1/admin/user-grades/deletable
     */
    @GetMapping("/deletable")
    @PreAuthorize("hasRole('PRESIDENT')")
    public ApiResponse<List<UserGradeResponse>> getDeletableGrades() {
        List<UserGrade> grades = userGradeService.getDeletableGrades();
        List<UserGradeResponse> response = grades.stream()
                .map(UserGradeResponse::from)
                .toList();
        return ApiResponse.success(response);
    }

    /**
     * 임원 등급 목록
     * Get executive grades
     *
     * GET /api/v1/admin/user-grades/executives
     */
    @GetMapping("/executives")
    @PreAuthorize("hasRole('DIRECTOR')")
    public ApiResponse<List<UserGradeResponse>> getExecutiveGrades() {
        List<UserGrade> grades = userGradeService.getExecutiveGrades();
        List<UserGradeResponse> response = grades.stream()
                .map(UserGradeResponse::from)
                .toList();
        return ApiResponse.success(response);
    }

    /**
     * 운영진 등급 목록
     * Get staff grades
     *
     * GET /api/v1/admin/user-grades/staff
     */
    @GetMapping("/staff")
    @PreAuthorize("hasRole('DIRECTOR')")
    public ApiResponse<List<UserGradeResponse>> getStaffGrades() {
        List<UserGrade> grades = userGradeService.getStaffGrades();
        List<UserGradeResponse> response = grades.stream()
                .map(UserGradeResponse::from)
                .toList();
        return ApiResponse.success(response);
    }
}
