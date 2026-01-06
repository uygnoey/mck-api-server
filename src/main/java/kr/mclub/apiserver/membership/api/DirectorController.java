package kr.mclub.apiserver.membership.api;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import kr.mclub.apiserver.membership.domain.DirectorPart;
import kr.mclub.apiserver.membership.service.DirectorPartService;
import kr.mclub.apiserver.shared.security.CurrentUser;
import kr.mclub.apiserver.shared.util.ApiResponse;

/**
 * 이사진 관리 Controller / Director Management Controller
 *
 * <p>이사진 파트(업무 분장) 관리를 위한 REST API를 제공합니다.</p>
 * <p>Provides REST API for managing director parts (work divisions).</p>
 *
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/membership/directors")
@RequiredArgsConstructor
public class DirectorController {

    private final DirectorPartService directorPartService;

    /**
     * 이사진 파트 생성 (관리자) / Create director part (admin)
     *
     * @param adminId 관리자 ID (인증된 관리자)
     * @param partName 파트명
     * @param description 설명
     * @param sortOrder 정렬 순서
     * @return 생성된 파트
     */
    @PostMapping("/parts")
    public ApiResponse<DirectorPart> createPart(
            @CurrentUser Long adminId,
            @RequestParam String partName,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "0") Integer sortOrder) {
        log.info("이사진 파트 생성: adminId={}, partName={}", adminId, partName);

        DirectorPart part = directorPartService.createPart(partName, description, sortOrder, adminId);

        return ApiResponse.success(part);
    }

    /**
     * 모든 이사진 파트 조회 / Get all director parts
     *
     * @return 파트 목록
     */
    @GetMapping("/parts")
    public ApiResponse<List<DirectorPart>> getAllParts() {
        log.info("모든 이사진 파트 조회");

        List<DirectorPart> parts = directorPartService.getAllParts();

        return ApiResponse.success(parts);
    }

    /**
     * 활성 이사진 파트 목록 조회 / Get active director parts
     *
     * @return 활성 파트 목록
     */
    @GetMapping("/parts/active")
    public ApiResponse<List<DirectorPart>> getActiveParts() {
        log.info("활성 이사진 파트 조회");

        List<DirectorPart> parts = directorPartService.getActiveParts();

        return ApiResponse.success(parts);
    }

    /**
     * 이사진 파트 ID로 조회 / Get director part by ID
     *
     * @param partId 파트 ID
     * @return 파트
     */
    @GetMapping("/parts/{partId}")
    public ApiResponse<DirectorPart> getPartById(@PathVariable Long partId) {
        log.info("이사진 파트 조회: partId={}", partId);

        DirectorPart part = directorPartService.getPartById(partId);

        return ApiResponse.success(part);
    }

    /**
     * 이사진 파트 정보 업데이트 (관리자) / Update director part information (admin)
     *
     * @param partId 파트 ID
     * @param partName 파트명
     * @param description 설명
     * @param sortOrder 정렬 순서
     * @return 업데이트된 파트
     */
    @PutMapping("/parts/{partId}")
    public ApiResponse<DirectorPart> updatePart(
            @PathVariable Long partId,
            @RequestParam(required = false) String partName,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer sortOrder) {
        log.info("이사진 파트 업데이트: partId={}", partId);

        DirectorPart part = directorPartService.updatePart(partId, partName, description, sortOrder);

        return ApiResponse.success(part);
    }

    /**
     * 이사진 파트 활성화 (관리자) / Activate director part (admin)
     *
     * @param partId 파트 ID
     * @return 활성화된 파트
     */
    @PostMapping("/parts/{partId}/activate")
    public ApiResponse<DirectorPart> activatePart(@PathVariable Long partId) {
        log.info("이사진 파트 활성화: partId={}", partId);

        DirectorPart part = directorPartService.activatePart(partId);

        return ApiResponse.success(part);
    }

    /**
     * 이사진 파트 비활성화 (관리자) / Deactivate director part (admin)
     *
     * @param partId 파트 ID
     * @return 비활성화된 파트
     */
    @PostMapping("/parts/{partId}/deactivate")
    public ApiResponse<DirectorPart> deactivatePart(@PathVariable Long partId) {
        log.info("이사진 파트 비활성화: partId={}", partId);

        DirectorPart part = directorPartService.deactivatePart(partId);

        return ApiResponse.success(part);
    }

    /**
     * 이사진 파트 삭제 (관리자) / Delete director part (admin)
     *
     * @param partId 파트 ID
     * @return 성공 응답
     */
    @DeleteMapping("/parts/{partId}")
    public ApiResponse<Void> deletePart(@PathVariable Long partId) {
        log.info("이사진 파트 삭제: partId={}", partId);

        directorPartService.deletePart(partId);

        return ApiResponse.success(null);
    }

    /**
     * 이사진 파트의 표준 권한 설정 (관리자) / Set standard permissions for director part (admin)
     *
     * @param partId 파트 ID
     * @param canManageMembers 회원 관리 권한
     * @param canManageEvents 행사 관리 권한
     * @param canManageBoards 게시판 관리 권한
     * @param canManagePayments 결제 관리 권한
     * @return 권한이 설정된 파트
     */
    @PostMapping("/parts/{partId}/permissions")
    public ApiResponse<DirectorPart> setPermissions(
            @PathVariable Long partId,
            @RequestParam(defaultValue = "false") boolean canManageMembers,
            @RequestParam(defaultValue = "false") boolean canManageEvents,
            @RequestParam(defaultValue = "false") boolean canManageBoards,
            @RequestParam(defaultValue = "false") boolean canManagePayments) {
        log.info("이사진 파트 권한 설정: partId={}", partId);

        DirectorPart part = directorPartService.setPermissions(
                partId, canManageMembers, canManageEvents, canManageBoards, canManagePayments);

        return ApiResponse.success(part);
    }

    /**
     * 이사진 파트의 커스텀 권한 설정 (관리자) / Set custom permissions for director part (admin)
     *
     * @param partId 파트 ID
     * @param customPermissions 커스텀 권한 (JSON)
     * @return 권한이 설정된 파트
     */
    @PostMapping("/parts/{partId}/permissions/custom")
    public ApiResponse<DirectorPart> setCustomPermissions(
            @PathVariable Long partId,
            @RequestBody Map<String, Object> customPermissions) {
        log.info("이사진 파트 커스텀 권한 설정: partId={}", partId);

        DirectorPart part = directorPartService.setCustomPermissions(partId, customPermissions);

        return ApiResponse.success(part);
    }
}
