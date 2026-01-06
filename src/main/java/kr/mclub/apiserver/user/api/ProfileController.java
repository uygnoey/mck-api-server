package kr.mclub.apiserver.user.api;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import kr.mclub.apiserver.shared.security.CurrentUser;
import kr.mclub.apiserver.shared.util.ApiResponse;
import kr.mclub.apiserver.user.api.dto.EmailChangeRequest;
import kr.mclub.apiserver.user.api.dto.PhoneNumberChangeRequest;
import kr.mclub.apiserver.user.api.dto.RealNameChangeRequest;
import kr.mclub.apiserver.user.service.ProfileService;

/**
 * 프로필 관리 API 컨트롤러
 * Profile management API controller
 */
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /**
     * 프로필 이미지 업로드
     * Upload profile image
     *
     * POST /api/v1/profile/image
     */
    @PostMapping("/image")
    public ApiResponse<String> uploadProfileImage(
            @CurrentUser Long userId,
            @RequestParam("image") MultipartFile imageFile) {

        String imageUrl = profileService.uploadProfileImage(userId, imageFile);
        return ApiResponse.success(imageUrl);
    }

    /**
     * 프로필 이미지 삭제
     * Delete profile image
     *
     * DELETE /api/v1/profile/image
     */
    @DeleteMapping("/image")
    public ApiResponse<Void> deleteProfileImage(@CurrentUser Long userId) {
        profileService.deleteProfileImage(userId);
        return ApiResponse.success("프로필 이미지가 삭제되었습니다.");
    }

    /**
     * 실명 변경 신청
     * Request real name change
     *
     * POST /api/v1/profile/real-name
     */
    @PostMapping("/real-name")
    public ApiResponse<Void> requestRealNameChange(
            @CurrentUser Long userId,
            @Valid @RequestBody RealNameChangeRequest request) {

        profileService.requestRealNameChange(userId, request.newRealName(), request.reason());
        return ApiResponse.success("실명 변경이 신청되었습니다.");
    }

    /**
     * 전화번호 변경
     * Change phone number
     *
     * PUT /api/v1/profile/phone
     */
    @PutMapping("/phone")
    public ApiResponse<Void> changePhoneNumber(
            @CurrentUser Long userId,
            @Valid @RequestBody PhoneNumberChangeRequest request) {

        profileService.changePhoneNumber(userId, request.phoneNumber());
        return ApiResponse.success("전화번호가 변경되었습니다.");
    }

    /**
     * 이메일 변경
     * Change email
     *
     * PUT /api/v1/profile/email
     */
    @PutMapping("/email")
    public ApiResponse<Void> changeEmail(
            @CurrentUser Long userId,
            @Valid @RequestBody EmailChangeRequest request) {

        profileService.changeEmail(userId, request.email());
        return ApiResponse.success("이메일이 변경되었습니다.");
    }

    /**
     * 프로필 공개 설정 변경
     * Change profile visibility
     *
     * PUT /api/v1/profile/visibility
     */
    @PutMapping("/visibility")
    public ApiResponse<Void> changeProfileVisibility(
            @CurrentUser Long userId,
            @RequestParam boolean isPublic) {

        profileService.changeProfileVisibility(userId, isPublic);
        return ApiResponse.success("프로필 공개 설정이 변경되었습니다.");
    }
}
