package kr.mclub.apiserver.user.api;

import jakarta.validation.Valid;
import kr.mclub.apiserver.shared.security.CurrentUser;
import kr.mclub.apiserver.shared.util.ApiResponse;
import kr.mclub.apiserver.user.api.dto.ProfileUpdateRequest;
import kr.mclub.apiserver.user.api.dto.UserProfileResponse;
import kr.mclub.apiserver.user.domain.User;
import kr.mclub.apiserver.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 API 컨트롤러
 * User API controller
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 내 정보 조회
     * Get my profile
     *
     * GET /api/v1/users/me
     */
    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getMyProfile(@CurrentUser Long userId) {
        User user = userService.getUserById(userId);
        return ApiResponse.success(UserProfileResponse.from(user));
    }

    /**
     * 내 정보 수정
     * Update my profile
     *
     * PUT /api/v1/users/me
     */
    @PutMapping("/me")
    public ApiResponse<UserProfileResponse> updateMyProfile(
            @CurrentUser Long userId,
            @Valid @RequestBody ProfileUpdateRequest request) {

        User user = userService.updateProfile(
                userId,
                request.phoneNumber(),
                request.profileImageUrl()
        );

        return ApiResponse.success(UserProfileResponse.from(user), "프로필이 수정되었습니다.");
    }

    /**
     * 회원 탈퇴
     * Withdraw membership
     *
     * DELETE /api/v1/users/me
     */
    @DeleteMapping("/me")
    public ApiResponse<Void> withdraw(
            @CurrentUser Long userId,
            @RequestParam(required = false) String reason) {

        userService.withdraw(userId, reason);
        return ApiResponse.success("회원 탈퇴가 완료되었습니다.");
    }

    /**
     * 다른 사용자 프로필 조회 (정회원 번호로)
     * Get user profile by member number
     *
     * GET /api/v1/users/member/{memberNumber}
     */
    @GetMapping("/member/{memberNumber}")
    public ApiResponse<UserProfileResponse> getUserByMemberNumber(
            @PathVariable Integer memberNumber) {

        User user = userService.getUserByMemberNumber(memberNumber);

        // 제한된 정보만 반환 (개인정보 보호)
        return ApiResponse.success(UserProfileResponse.from(user));
    }
}
