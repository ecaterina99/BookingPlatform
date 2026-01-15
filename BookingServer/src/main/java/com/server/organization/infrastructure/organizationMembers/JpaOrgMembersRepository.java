package com.server.organization.infrastructure.organizationMembers;

import com.server.organization.domain.organizationMembers.OrganizationMember;
import com.server.organization.domain.organizationMembers.OrganizationMemberRepository;
import com.server.shared.infrastructure.UserMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaOrgMembersRepository implements OrganizationMemberRepository {

    private final OrgMemberRepositoryJpa orgMemberRepositoryJpa;
    private final UserMapper userMapper;

    public JpaOrgMembersRepository(OrgMemberRepositoryJpa orgMemberRepositoryJpa, UserMapper userMapper) {
        this.orgMemberRepositoryJpa = orgMemberRepositoryJpa;
        this.userMapper = userMapper;
    }

    @Override
    public Optional<OrganizationMember> findById(int id) {
        return orgMemberRepositoryJpa.findById(id).map(userMapper::ToDomain);
    }

    @Override
    public List<OrganizationMember> findByOrganizationId(int organizationId) {
        return orgMemberRepositoryJpa.findByOrganizationId(organizationId).stream().map(userMapper::ToDomain).toList();
    }

    @Override
    public Optional<OrganizationMember> findByOrganizationIdAndUserId(int organizationId, int memberId) {
        return orgMemberRepositoryJpa.findByOrganizationIdAndUserId(organizationId, memberId).map(userMapper::ToDomain);
    }

    @Override
    public OrganizationMember save(OrganizationMember member) {
        OrganizationMembersEntity entity = userMapper.ToEntity(member);
        return userMapper.ToDomain(orgMemberRepositoryJpa.save(entity));
    }

    @Override
    public void delete(OrganizationMember member) {
        orgMemberRepositoryJpa.deleteById(member.getId());
    }
}
