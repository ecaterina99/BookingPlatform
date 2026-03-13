package com.server.service.infrastructure;

import com.server.service.domain.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceJpaRepository extends JpaRepository<ServiceJpaEntity, Integer> {
    List<ServiceJpaEntity> findByCategory(ServiceCategory category);

    List<ServiceJpaEntity> findByOrganizationId(int organizationId);
}
