package kr.mclub.apiserver.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.mclub.apiserver.user.domain.User;

/**
 * 사용자 Repository
 * User repository
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 조회
     * Find by email
     */
    Optional<User> findByEmail(String email);

    /**
     * 정회원 번호로 조회
     * Find by member number
     */
    @Query("SELECT u FROM User u JOIN FETCH u.grade WHERE u.memberNumber = :memberNumber")
    Optional<User> findByMemberNumber(@Param("memberNumber") Integer memberNumber);

    /**
     * 전화번호로 조회
     * Find by phone number
     */
    Optional<User> findByPhoneNumber(String phoneNumber);

    /**
     * 이메일 존재 여부
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * 전화번호 존재 여부
     * Check if phone number exists
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * 정회원 번호 존재 여부
     * Check if member number exists
     */
    boolean existsByMemberNumber(Integer memberNumber);

    /**
     * 등급 ID로 사용자 존재 여부 (등급 삭제 가능 여부 체크)
     * Check if any user exists with given grade ID
     */
    boolean existsByGradeId(Long gradeId);

    /**
     * 활성 사용자 중 특정 등급 사용자 목록
     * Find active users by grade code
     */
    @Query("SELECT u FROM User u JOIN u.grade g WHERE g.code = :gradeCode AND u.isWithdrawn = false AND u.isActive = true")
    List<User> findActiveUsersByGradeCode(@Param("gradeCode") String gradeCode);

    /**
     * 활성 사용자 중 특정 등급 사용자 페이지
     * Find active users by grade code (paged)
     */
    @Query("SELECT u FROM User u JOIN u.grade g WHERE g.code = :gradeCode AND u.isWithdrawn = false AND u.isActive = true")
    Page<User> findActiveUsersByGradeCode(@Param("gradeCode") String gradeCode, Pageable pageable);

    /**
     * 연회비 갱신 대상 정회원 조회
     * Find regular members for annual fee renewal
     */
    @Query("""
        SELECT u FROM User u
        JOIN u.grade g
        WHERE g.code = 'REGULAR'
        AND g.requiresAnnualFee = true
        AND u.exemptionType = 'NONE'
        AND u.isWithdrawn = false
        AND u.isActive = true
        """)
    List<User> findRegularMembersForRenewal();

    /**
     * 이름으로 검색 (부분 일치)
     * Search by real name (partial match)
     */
    @Query("SELECT u FROM User u WHERE u.realName LIKE %:name% AND u.isWithdrawn = false")
    Page<User> searchByRealName(@Param("name") String name, Pageable pageable);

    /**
     * 이름 또는 이메일로 검색
     * Search by real name or email
     */
    @Query("SELECT u FROM User u WHERE (u.realName LIKE %:keyword% OR u.email LIKE %:keyword%) AND u.isWithdrawn = false")
    Page<User> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 다음 정회원 번호 조회
     * Get next member number
     */
    @Query("SELECT COALESCE(MAX(u.memberNumber), 649) + 1 FROM User u")
    Integer getNextMemberNumber();

    /**
     * 탈퇴하지 않은 사용자 조회 (등급 정보 포함)
     * Find by ID excluding withdrawn users (with grade)
     */
    @Query("SELECT u FROM User u JOIN FETCH u.grade WHERE u.id = :id AND u.isWithdrawn = false")
    Optional<User> findByIdAndNotWithdrawn(@Param("id") Long id);
}
