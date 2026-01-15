package com.server.organization.infrastructure.organizationMembers;

import com.server.organization.domain.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name="organization_members")
public class OrganizationMembersEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false)
    private int organizationId;
    @Column(nullable = false)
    private int memberId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public OrganizationMembersEntity() {
    }

    public OrganizationMembersEntity(int organizationId, int memberId, Role role) {
        this.organizationId = organizationId;
        this.memberId = memberId;
        this.role = role;
    }

}
