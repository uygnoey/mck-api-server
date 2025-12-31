package kr.mclub.apiserver.shared.repository;

import kr.mclub.apiserver.shared.domain.CommonCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 공통 코드 Repository / Common Code Repository
 *
 * @since 1.0
 */
public interface CommonCodeRepository extends JpaRepository<CommonCode, Long> {

    /**
     * 코드 그룹과 코드로 조회
     *
     * @param codeGroup 코드 그룹
     * @param code 코드 값
     * @return 공통 코드
     */
    Optional<CommonCode> findByCodeGroupAndCode(String codeGroup, String code);

    /**
     * 코드 그룹의 모든 활성 코드 조회 (정렬 순서대로)
     *
     * @param codeGroup 코드 그룹
     * @return 공통 코드 목록
     */
    @Query("SELECT c FROM CommonCode c WHERE c.codeGroup = :codeGroup AND c.isActive = true ORDER BY c.displayOrder ASC")
    List<CommonCode> findActiveCodesByGroup(@Param("codeGroup") String codeGroup);

    /**
     * 코드 그룹의 모든 코드 조회 (활성/비활성 모두, 정렬 순서대로)
     *
     * @param codeGroup 코드 그룹
     * @return 공통 코드 목록
     */
    List<CommonCode> findByCodeGroupOrderByDisplayOrderAsc(String codeGroup);

    /**
     * 코드 그룹과 코드 존재 여부 확인
     *
     * @param codeGroup 코드 그룹
     * @param code 코드 값
     * @return 존재 여부
     */
    boolean existsByCodeGroupAndCode(String codeGroup, String code);

    /**
     * 모든 코드 그룹 목록 조회
     *
     * @return 코드 그룹 목록 (중복 제거)
     */
    @Query("SELECT DISTINCT c.codeGroup FROM CommonCode c ORDER BY c.codeGroup")
    List<String> findAllCodeGroups();
}
