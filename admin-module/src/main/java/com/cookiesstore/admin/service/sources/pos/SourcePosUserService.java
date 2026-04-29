package com.cookiesstore.admin.service.sources.pos;

import com.cookiesstore.admin.domain.AdminUser;
import com.cookiesstore.admin.domain.pos.AdminSourcePosUser;
import com.cookiesstore.admin.domain.pos.PosOperatorRole;
import com.cookiesstore.admin.repository.AdminUserRepository;
import com.cookiesstore.admin.repository.pos.AdminSourcePosUserRepository;
import com.cookiesstore.common.repositories.SourceRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SourcePosUserService {

    private final AdminSourcePosUserRepository sourcePosUserRepository;
    private final SourceRepository sourceRepository;
    private final AdminUserRepository adminUserRepository;

    public SourcePosUserService(
        AdminSourcePosUserRepository sourcePosUserRepository,
        SourceRepository sourceRepository,
        AdminUserRepository adminUserRepository
    ) {
        this.sourcePosUserRepository = sourcePosUserRepository;
        this.sourceRepository = sourceRepository;
        this.adminUserRepository = adminUserRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminSourcePosUser> listAssignedUsers(Long sourceId) {
        ensureSourceExists(sourceId);
        return sourcePosUserRepository.findBySourceIdOrderByCreatedAtDesc(sourceId);
    }

    @Transactional(readOnly = true)
    public List<AdminUser> listAvailableUsers(Long sourceId) {
        ensureSourceExists(sourceId);
        return adminUserRepository.findByActiveTrueOrderByCreatedAtDesc()
            .stream()
            .filter(user -> !sourcePosUserRepository.existsBySourceIdAndAdminUserId(sourceId, user.getId()))
            .toList();
    }

    public AdminSourcePosUser assignUser(
        Long sourceId,
        Long adminUserId,
        PosOperatorRole role,
        Long actorUserId
    ) {
        var source = sourceRepository.findById(sourceId)
            .orElseThrow(() -> new SourcePosUserSourceNotFoundException(sourceId));
        var adminUser = adminUserRepository.findById(adminUserId)
            .orElseThrow(() -> new SourcePosUserAdminUserNotFoundException(adminUserId));

        if (!adminUser.isActive()) {
            throw new SourcePosUserAdminUserInactiveException(adminUserId);
        }

        if (sourcePosUserRepository.existsBySourceIdAndAdminUserId(sourceId, adminUserId)) {
            throw new SourcePosUserAlreadyAssignedException(sourceId, adminUserId);
        }

        AdminSourcePosUser relation = new AdminSourcePosUser();
        relation.setSource(source);
        relation.setAdminUser(adminUser);
        relation.setRole(role);
        relation.setActive(true);
        relation.setAssignedByAdminUserId(actorUserId);
        return sourcePosUserRepository.save(relation);
    }

    public void removeUser(Long sourceId, Long relationId) {
        ensureSourceExists(sourceId);
        AdminSourcePosUser relation = sourcePosUserRepository.findById(relationId)
            .orElseThrow(() -> new SourcePosUserAssignmentNotFoundException(relationId));

        if (!relation.getSource().getId().equals(sourceId)) {
            throw new SourcePosUserSourceMismatchException(sourceId, relationId);
        }

        sourcePosUserRepository.delete(relation);
    }

    private void ensureSourceExists(Long sourceId) {
        if (!sourceRepository.existsById(sourceId)) {
            throw new SourcePosUserSourceNotFoundException(sourceId);
        }
    }
}
