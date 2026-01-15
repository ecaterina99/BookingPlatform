package com.server.shared.infrastructure;

import com.server.organization.api.OrganizationDTO;
import com.server.organization.api.UserDTO;
import com.server.organization.domain.organizationMembers.OrganizationMember;
import com.server.organization.domain.organizations.Organization;
import com.server.organization.domain.organizations.OrganizationAddress;
import com.server.organization.domain.organizations.OrganizationEmail;
import com.server.organization.domain.organizations.OrganizationPhone;
import com.server.organization.domain.users.User;
import com.server.organization.domain.users.UserEmail;
import com.server.organization.domain.users.UserPassword;
import com.server.organization.infrastructure.organizationMembers.OrganizationMembersEntity;
import com.server.organization.infrastructure.organizations.OrganizationJpaEntity;
import com.server.organization.infrastructure.users.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getEmail().value(),
                user.getFullName(),
                user.getGlobalRole().name()
        );
    }

    public UserJpaEntity toEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity(
                user.getEmail().value(),
                user.getPassword().value(),
                user.getFullName(),
                user.getGlobalRole()
        );
        entity.setId(user.getId());
        return entity;
    }

    public User toDomain(UserJpaEntity entity) {
        return new User(
                entity.getId(),
                new UserEmail(entity.getEmail()),
                new UserPassword(entity.getPassword()),
                entity.getFullName(),
                entity.getGlobalRole()
        );
    }

    public OrganizationJpaEntity toEntity(Organization organization) {
        OrganizationJpaEntity entity = new OrganizationJpaEntity(
                organization.getName(),
                organization.getEmail().value(),
                organization.getCity(),
                organization.getAddress().value(),
                organization.getPhone().value()

        );
        entity.setId(organization.getId());
        return entity;
    }

    public Organization toDomain(OrganizationJpaEntity entity) {
        return new Organization(
                entity.getId(),
                entity.getName(),
                new OrganizationAddress(entity.getAddress()),
                entity.getCity(),
                new OrganizationEmail(entity.getEmail()),
                new OrganizationPhone(entity.getPhone())
        );
    }

    public OrganizationDTO toDTO(Organization organization) {
        return new OrganizationDTO(
                organization.getId(),
                organization.getName(),
                organization.getCity(),
                organization.getAddress().value(),
                organization.getPhone().value(),
                organization.getEmail().value()
        );
    }

    public OrganizationMember orgMemberToDomain(OrganizationMembersEntity e) {
        return new OrganizationMember(
                e.getId(),
                e.getOrganizationId(),
                e.getMemberId(),
                e.getRole());
    }

    public OrganizationMembersEntity domainToEntity(OrganizationMember m) {
        OrganizationMembersEntity e = new OrganizationMembersEntity(
        m.getOrganizationId(),
        m.getUserId(),
        m.getRole()
        );
        e.setId(m.getId());
        return e;
    }
}