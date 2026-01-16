package com.server.services.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceJpaRepository extends JpaRepository<ServiceJpaEntity, Integer> {
}
