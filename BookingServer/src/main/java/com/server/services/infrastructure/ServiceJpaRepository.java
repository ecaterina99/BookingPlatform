package com.server.services.infrastructure;

import com.server.organization.infrastructure.organizations.OrganizationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceJpaRepository extends JpaRepository<ServiceJpaEntity, Integer> {
}
