package kr.mclub.apiserver.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import kr.mclub.apiserver.shared.exception.BusinessException;
import kr.mclub.apiserver.shared.exception.ErrorCode;
import kr.mclub.apiserver.user.domain.User;
import kr.mclub.apiserver.user.repository.UserRepository;

/**
 * 프로필 관리 서비스
 * Profile management service
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final UserRepository userRepository;
    // TODO: FileUploadService 추가 필요 (프로필 이미지 업로드)

    /**
     * 프로필 이미지 업로드
     * Upload profile image
     *
     * @param userId 사용자 ID
     * @param imageFile 이미지 파일
     * @return 업로드된 이미지 URL
     */
    @Transactional
    public String uploadProfileImage(Long userId, MultipartFile imageFile) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // TODO: FileUploadService를 통해 이미지 업로드
        // 임시로 더미 URL 반환
        String imageUrl = "https://placeholder.com/profile/" + userId;

        user.updateProfileImage(imageUrl);
        log.info("Profile image uploaded: userId={}, url={}", userId, imageUrl);

        return imageUrl;
    }

    /**
     * 프로필 이미지 삭제
     * Delete profile image
     *
     * @param userId 사용자 ID
     */
    @Transactional
    public void deleteProfileImage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // TODO: FileUploadService를 통해 기존 이미지 삭제

        user.updateProfileImage(null);
        log.info("Profile image deleted: userId={}", userId);
    }

    /**
     * 실명 변경 (관리자 승인 필요)
     * Change real name (requires admin approval)
     *
     * @param userId 사용자 ID
     * @param newRealName 새 실명
     * @param reason 변경 사유
     */
    @Transactional
    public void requestRealNameChange(Long userId, String newRealName, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // TODO: 실명 변경 신청 로직 구현
        // 현재는 바로 변경 (추후 승인 프로세스 추가 필요)
        user.updateRealName(newRealName);
        log.info("Real name change requested: userId={}, oldName={}, newName={}, reason={}",
                userId, user.getRealName(), newRealName, reason);
    }

    /**
     * 전화번호 변경
     * Change phone number
     *
     * @param userId 사용자 ID
     * @param newPhoneNumber 새 전화번호
     */
    @Transactional
    public void changePhoneNumber(Long userId, String newPhoneNumber) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 전화번호 중복 확인
        if (userRepository.existsByPhoneNumber(newPhoneNumber)) {
            throw new BusinessException(ErrorCode.DUPLICATE_PHONE);
        }

        user.updatePhoneNumber(newPhoneNumber);
        log.info("Phone number changed: userId={}, newPhoneNumber={}", userId, newPhoneNumber);
    }

    /**
     * 이메일 변경 (인증 필요)
     * Change email (requires verification)
     *
     * @param userId 사용자 ID
     * @param newEmail 새 이메일
     */
    @Transactional
    public void changeEmail(Long userId, String newEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 이메일 중복 확인
        if (userRepository.existsByEmail(newEmail)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        // TODO: 이메일 인증 프로세스 추가 필요
        user.updateEmail(newEmail);
        log.info("Email changed: userId={}, newEmail={}", userId, newEmail);
    }

    /**
     * 프로필 공개 설정 변경
     * Change profile visibility
     *
     * @param userId 사용자 ID
     * @param isPublic 공개 여부
     */
    @Transactional
    public void changeProfileVisibility(Long userId, boolean isPublic) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.setProfilePublic(isPublic);
        log.info("Profile visibility changed: userId={}, isPublic={}", userId, isPublic);
    }
}
