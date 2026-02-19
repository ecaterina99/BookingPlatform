package com.server.organization.infrastructure.organizationMembers;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrgMemberRepositoryJpa extends JpaRepository<OrganizationMembersEntity, Integer> {
    List<OrganizationMembersEntity> findByOrganizationId(int organizationId);

    Optional<OrganizationMembersEntity> findByOrganizationIdAndUserId(int orgId, int userId);

    List<OrganizationMembersEntity> findByUserId(int userId);

}
