package com.botomosy.srm1.domain.tenant;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "tenants",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tenant_slug", columnNames = "slug")
        }
)
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 100)
    private String slug;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, length = 50)
    private String productCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TenantPlan plan = TenantPlan.BASIC;

    @Column(nullable = false)
    private int studentLimit = 100;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Tenant() {
    }

    public Tenant(String name, String slug, boolean active, String productCode) {
        this.name = name;
        this.slug = slug;
        this.active = active;
        this.productCode = productCode;
        this.plan = TenantPlan.BASIC;
        this.studentLimit = 100;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (plan == null) {
            plan = TenantPlan.BASIC;
        }
        if (studentLimit <= 0) {
            studentLimit = 100;
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public boolean isActive() {
        return active;
    }

    public String getProductCode() {
        return productCode;
    }

    public TenantPlan getPlan() {
        return plan;
    }

    public int getStudentLimit() {
        return studentLimit;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public void setPlan(TenantPlan plan) {
        this.plan = plan;
    }

    public void setStudentLimit(int studentLimit) {
        this.studentLimit = studentLimit;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}