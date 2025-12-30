package kr.mclub.apiserver.user.api;

import kr.mclub.apiserver.shared.util.ApiResponse;
import kr.mclub.apiserver.user.api.dto.UserGradeResponse;
import kr.mclub.apiserver.user.service.UserGradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 사용자 등급 API 컨트롤러 (공개)
 * User grade API controller (public endpoints)
 */
@RestController
@RequestMapping("/api/v1/grades")
@RequiredArgsConstructor
public class UserGradeController {

    private final UserGradeService userGradeService;

    /**
     * 모든 등급 목록 조회
     * Get all grades
     *
     * GET /api/v1/grades
     */
    @GetMapping
    public ApiResponse<List<UserGradeResponse>> getAllGrades() {
        var grades = userGradeService.getAllActiveGrades().stream()
                .map(UserGradeResponse::from)
                .toList();

        return ApiResponse.success(grades);
    }
}
