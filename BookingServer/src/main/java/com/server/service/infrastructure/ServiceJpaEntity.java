package com.server.service.infrastructure;

import com.server.service.domain.ServiceCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "services")
public class ServiceJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(nullable = false, name = "organization_id")
    private int organizationId;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false, name = "duration_minutes")
    private int duration;
    private int price;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'OTHER'")
    private ServiceCategory category;

    public ServiceJpaEntity() {
    }

    public ServiceJpaEntity(String name, int organizationId, String description, int duration, int price, ServiceCategory category) {
        this.name = name;
        this.organizationId = organizationId;
        this.description = description;
        this.duration = duration;
        this.price = price;
        this.category = category;
    }
}
