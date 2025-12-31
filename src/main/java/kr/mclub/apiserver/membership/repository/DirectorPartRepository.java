package kr.mclub.apiserver.membership.repository;

import kr.mclub.apiserver.membership.domain.DirectorPart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 이사 파트 마스터 Repository / Director Part Master Repository
 *
 * DirectorPart는 "파트 마스터" 테이블로, 파트의 정의를 관리합니다.
 * DirectorPart is a "master table" that manages part definitions.
 *
 * @since 1.0
 */
public interface DirectorPartRepository extends JpaRepository<DirectorPart, Long> {

    /**
     * 파트명으로 파트 조회 / Find part by part name
     */
    Optional<DirectorPart> findByPartName(String partName);

    /**
     * 활성 파트 목록 조회 (정렬순) / Find all active parts ordered by display order
     */
    List<DirectorPart> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * 모든 파트 목록 조회 (정렬순) / Find all parts ordered by display order
     */
    List<DirectorPart> findAllByOrderByDisplayOrderAsc();

    /**
     * 파트명 존재 여부 확인 / Check if part name exists
     */
    boolean existsByPartName(String partName);

    /**
     * 활성 파트 존재 여부 확인 / Check if active part exists
     */
    boolean existsByPartNameAndIsActiveTrue(String partName);
}
