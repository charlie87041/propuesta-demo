package com.cookiesstore.admin.service;

import com.cookiesstore.common.authorization.domain.Ability;
import com.cookiesstore.common.authorization.domain.Domain;
import com.cookiesstore.common.authorization.domain.UserDomainAbility;
import com.cookiesstore.common.authorization.repository.AbilityRepository;
import com.cookiesstore.common.authorization.repository.DomainRepository;
import com.cookiesstore.common.authorization.repository.UserDomainAbilityRepository;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminAbilityAssignmentService {

    private final UserDomainAbilityRepository userDomainAbilityRepository;
    private final DomainRepository domainRepository;
    private final AbilityRepository abilityRepository;

    public AdminAbilityAssignmentService(
        UserDomainAbilityRepository userDomainAbilityRepository,
        DomainRepository domainRepository,
        AbilityRepository abilityRepository
    ) {
        this.userDomainAbilityRepository = userDomainAbilityRepository;
        this.domainRepository = domainRepository;
        this.abilityRepository = abilityRepository;
    }

    public void assignAbility(Long grantedByUserId, Long targetUserId, String domainCode, String abilityCode) {
        Domain domain = domainRepository.findByCode(domainCode)
            .orElseThrow(() -> new IllegalArgumentException("Domain not found: " + domainCode));
        Ability ability = abilityRepository.findByCode(abilityCode)
            .orElseThrow(() -> new IllegalArgumentException("Ability not found: " + abilityCode));

        UserDomainAbility assignment = userDomainAbilityRepository
            .findByUserIdAndDomainCodeAndAbilityCode(targetUserId, domainCode, abilityCode)
            .orElseGet(UserDomainAbility::new);

        assignment.setUserId(targetUserId);
        assignment.setDomain(domain);
        assignment.setAbility(ability);
        assignment.setGranted(true);
        assignment.setGrantedBy(grantedByUserId);

        userDomainAbilityRepository.save(assignment);
    }

    public void setSingleRole(Long actorUserId, Long targetUserId, String domainCode, String abilityCode) {
        for (UserDomainAbility assignment : userDomainAbilityRepository.findByUserIdAndDomainCodeAndGrantedTrue(targetUserId, domainCode)) {
            if (!assignment.getAbility().getCode().equals(abilityCode)) {
                assignment.setGranted(false);
                assignment.setGrantedBy(actorUserId);
                userDomainAbilityRepository.save(assignment);
            }
        }

        assignAbility(actorUserId, targetUserId, domainCode, abilityCode);
    }

    public void revokeAbility(Long actorUserId, Long targetUserId, String domainCode, String abilityCode) {
        UserDomainAbility assignment = userDomainAbilityRepository
            .findByUserIdAndDomainCodeAndAbilityCode(targetUserId, domainCode, abilityCode)
            .orElseThrow(() -> new IllegalArgumentException("Ability assignment not found"));

        if (actorUserId.equals(targetUserId) && "super-admin".equals(abilityCode) && assignment.isGranted()) {
            long activeSuperAdminAssignments = userDomainAbilityRepository
                .countByUserIdAndDomainCodeAndAbilityCodeAndGrantedTrue(targetUserId, domainCode, "super-admin");
            if (activeSuperAdminAssignments <= 1) {
                throw new IllegalStateException("Cannot revoke the last super-admin ability from yourself");
            }
        }

        assignment.setGranted(false);
        assignment.setGrantedBy(actorUserId);
        userDomainAbilityRepository.save(assignment);
    }

    public Set<String> listAbilityCodes(Long userId, String domainCode) {
        return userDomainAbilityRepository.findByUserIdAndDomainCodeAndGrantedTrue(userId, domainCode)
            .stream()
            .map(assignment -> assignment.getAbility().getCode())
            .collect(Collectors.toSet());
    }

    public void revokeAllAbilitiesForUser(Long userId) {
        for (UserDomainAbility assignment : userDomainAbilityRepository.findByUserIdAndGrantedTrue(userId)) {
            assignment.setGranted(false);
            userDomainAbilityRepository.save(assignment);
        }
    }
}
