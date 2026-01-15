package com.server.organization.infrastructure.organizationMembers;

import com.server.organization.domain.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "organization_members")
public class OrganizationMembersEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false, name = "organization_id")
    private int organizationId;
    @Column(nullable = false, name = "user_id")
    private int userId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public OrganizationMembersEntity() {
    }

    public OrganizationMembersEntity(int organizationId, int userId, Role role) {
        this.organizationId = organizationId;
        this.userId = userId;
        this.role = role;
    }

}
