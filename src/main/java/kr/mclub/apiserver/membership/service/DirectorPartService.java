package kr.mclub.apiserver.membership.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import kr.mclub.apiserver.membership.domain.DirectorPart;
import kr.mclub.apiserver.membership.repository.DirectorPartRepository;
import kr.mclub.apiserver.shared.exception.BusinessException;
import kr.mclub.apiserver.shared.exception.ErrorCode;

/**
 * 이사 파트 관리 Service / Director Part Management Service
 *
 * <p>이사진의 파트(업무 분담) 정보를 관리합니다.</p>
 * <p>Manages director parts (work division) information.</p>
 *
 * <p>파트 예시: 행사, 홍보, 총무, 재무, 기술 등</p>
 * <p>Part examples: Events, PR, General Affairs, Finance, Tech, etc.</p>
 *
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectorPartService {

    private final DirectorPartRepository partRepository;

    /**
     * 파트 생성 / Create part
     *
     * @param name 파트명
     * @param description 파트 설명
     * @param displayOrder 표시 순서
     * @param createdBy 생성자 ID (회장)
     * @return 생성된 파트
     * @throws BusinessException 이미 존재하는 파트명인 경우
     */
    @Transactional
    public DirectorPart createPart(String name, String description, Integer displayOrder, Long createdBy) {
        log.info("파트 생성 시작: name={}, createdBy={}", name, createdBy);

        // 중복 확인
        if (partRepository.existsByName(name)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    String.format("이미 존재하는 파트명입니다: %s", name));
        }

        DirectorPart part = DirectorPart.builder()
                .name(name)
                .description(description)
                .displayOrder(displayOrder)
                .createdBy(createdBy)
                .build();

        DirectorPart savedPart = partRepository.save(part);
        log.info("파트 생성 완료: partId={}, name={}", savedPart.getId(), savedPart.getName());

        return savedPart;
    }

    /**
     * 파트 정보 업데이트 / Update part information
     *
     * @param partId 파트 ID
     * @param name 파트명
     * @param description 파트 설명
     * @param displayOrder 표시 순서
     * @return 업데이트된 파트
     * @throws BusinessException 파트를 찾을 수 없거나 중복된 파트명인 경우
     */
    @Transactional
    public DirectorPart updatePart(Long partId, String name, String description, Integer displayOrder) {
        log.info("파트 정보 업데이트: partId={}, name={}", partId, name);

        DirectorPart part = partRepository.findById(partId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "파트를 찾을 수 없습니다"));

        // 파트명 변경 시 중복 확인
        if (!part.getName().equals(name) && partRepository.existsByName(name)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    String.format("이미 존재하는 파트명입니다: %s", name));
        }

        part.update(name, description, displayOrder);

        log.info("파트 정보 업데이트 완료: partId={}", partId);
        return part;
    }

    /**
     * 파트 권한 설정 / Set part permissions
     *
     * @param partId 파트 ID
     * @param canManageMembers 회원 관리 권한
     * @param canManagePosts 게시글 관리 권한
     * @param canManageEvents 이벤트 관리 권한
     * @param canAssignSubPermissions 세부 권한 지정 가능
     * @return 권한이 설정된 파트
     * @throws BusinessException 파트를 찾을 수 없는 경우
     */
    @Transactional
    public DirectorPart setPermissions(Long partId, boolean canManageMembers, boolean canManagePosts,
                                       boolean canManageEvents, boolean canAssignSubPermissions) {
        log.info("파트 권한 설정: partId={}, members={}, posts={}, events={}, subPerms={}",
                partId, canManageMembers, canManagePosts, canManageEvents, canAssignSubPermissions);

        DirectorPart part = partRepository.findById(partId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "파트를 찾을 수 없습니다"));

        part.setPermissions(canManageMembers, canManagePosts, canManageEvents, canAssignSubPermissions);

        log.info("파트 권한 설정 완료: partId={}", partId);
        return part;
    }

    /**
     * 파트 커스텀 권한 설정 / Set custom permissions
     *
     * @param partId 파트 ID
     * @param customPermissions 커스텀 권한 (JSON)
     * @return 커스텀 권한이 설정된 파트
     * @throws BusinessException 파트를 찾을 수 없는 경우
     */
    @Transactional
    public DirectorPart setCustomPermissions(Long partId, Map<String, Object> customPermissions) {
        log.info("파트 커스텀 권한 설정: partId={}, permissions={}", partId, customPermissions);

        DirectorPart part = partRepository.findById(partId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "파트를 찾을 수 없습니다"));

        part.setCustomPermissions(customPermissions);

        log.info("파트 커스텀 권한 설정 완료: partId={}", partId);
        return part;
    }

    /**
     * 파트 활성화 / Activate part
     *
     * @param partId 파트 ID
     * @return 활성화된 파트
     * @throws BusinessException 파트를 찾을 수 없는 경우
     */
    @Transactional
    public DirectorPart activatePart(Long partId) {
        log.info("파트 활성화: partId={}", partId);

        DirectorPart part = partRepository.findById(partId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "파트를 찾을 수 없습니다"));

        part.activate();

        log.info("파트 활성화 완료: partId={}", partId);
        return part;
    }

    /**
     * 파트 비활성화 / Deactivate part
     *
     * @param partId 파트 ID
     * @return 비활성화된 파트
     * @throws BusinessException 파트를 찾을 수 없는 경우
     */
    @Transactional
    public DirectorPart deactivatePart(Long partId) {
        log.info("파트 비활성화: partId={}", partId);

        DirectorPart part = partRepository.findById(partId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "파트를 찾을 수 없습니다"));

        part.deactivate();

        log.info("파트 비활성화 완료: partId={}", partId);
        return part;
    }

    /**
     * 파트 삭제 / Delete part
     *
     * <p>실제로는 데이터베이스에서 삭제하지 않고 비활성화합니다.</p>
     * <p>Does not actually delete from database, just deactivates.</p>
     *
     * @param partId 파트 ID
     * @throws BusinessException 파트를 찾을 수 없는 경우
     */
    @Transactional
    public void deletePart(Long partId) {
        log.info("파트 삭제 (비활성화): partId={}", partId);

        DirectorPart part = partRepository.findById(partId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "파트를 찾을 수 없습니다"));

        // TODO: 해당 파트에 배정된 이사가 있는지 확인
        // User Module에서 director_part_id를 참조하는 이사가 있으면 삭제 불가

        part.deactivate();

        log.info("파트 삭제 (비활성화) 완료: partId={}", partId);
    }

    /**
     * 모든 파트 조회 (정렬순) / Get all parts ordered by display order
     *
     * @return 파트 목록
     */
    public List<DirectorPart> getAllParts() {
        return partRepository.findAllByOrderByDisplayOrderAsc();
    }

    /**
     * 활성 파트 목록 조회 (정렬순) / Get all active parts ordered by display order
     *
     * @return 활성 파트 목록
     */
    public List<DirectorPart> getActiveParts() {
        return partRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    /**
     * 파트 ID로 조회 / Get part by ID
     *
     * @param partId 파트 ID
     * @return 파트
     * @throws BusinessException 파트를 찾을 수 없는 경우
     */
    public DirectorPart getPartById(Long partId) {
        return partRepository.findById(partId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "파트를 찾을 수 없습니다"));
    }

    /**
     * 파트명으로 조회 / Get part by name
     *
     * @param name 파트명
     * @return 파트
     * @throws BusinessException 파트를 찾을 수 없는 경우
     */
    public DirectorPart getPartByName(String name) {
        return partRepository.findByName(name)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        String.format("파트를 찾을 수 없습니다: %s", name)));
    }

    /**
     * 파트명 존재 여부 확인 / Check if part name exists
     *
     * @param name 파트명
     * @return 존재 여부
     */
    public boolean existsByName(String name) {
        return partRepository.existsByName(name);
    }

    /**
     * 활성 파트 존재 여부 확인 / Check if active part exists
     *
     * @param name 파트명
     * @return 활성 파트 존재 여부
     */
    public boolean existsActiveByName(String name) {
        return partRepository.existsByNameAndIsActiveTrue(name);
    }
}
