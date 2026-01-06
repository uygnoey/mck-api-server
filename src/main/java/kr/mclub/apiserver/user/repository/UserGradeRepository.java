package kr.mclub.apiserver.user.repository;

import kr.mclub.apiserver.user.domain.UserGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 등급 Repository
 * User grade repository
 */
public interface UserGradeRepository extends JpaRepository<UserGrade, Long> {

    /**
     * 등급 코드로 조회
     * Find by grade code
     */
    Optional<UserGrade> findByCode(String code);

    /**
     * Spring Security Role 이름으로 조회
     * Find by role name
     */
    Optional<UserGrade> findByRoleName(String roleName);

    /**
     * 활성 등급 목록 (표시 순서대로)
     * Find all active grades ordered by display order
     */
    List<UserGrade> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * 삭제 가능한 등급 목록 (시스템 등급 제외)
     * Find deletable grades (excluding system grades)
     */
    @Query("SELECT g FROM UserGrade g WHERE g.isSystemGrade = false AND g.isActive = true ORDER BY g.displayOrder")
    List<UserGrade> findDeletableGrades();

    /**
     * 등급 코드 존재 여부
     * Check if grade code exists
     */
    boolean existsByCode(String code);

    /**
     * Role 이름 존재 여부
     * Check if role name exists
     */
    boolean existsByRoleName(String roleName);

    /**
     * 권한 레벨 이상인 등급 목록
     * Find grades with permission level >= given level
     */
    @Query("SELECT g FROM UserGrade g WHERE g.permissionLevel >= :level AND g.isActive = true ORDER BY g.permissionLevel DESC")
    List<UserGrade> findByPermissionLevelGreaterThanEqual(@Param("level") Integer level);

    /**
     * 임원 등급 목록
     * Find executive grades
     */
    List<UserGrade> findByIsExecutiveTrueAndIsActiveTrue();

    /**
     * 운영진 등급 목록
     * Find staff grades
     */
    List<UserGrade> findByIsStaffTrueAndIsActiveTrue();
}
