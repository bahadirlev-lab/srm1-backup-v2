package com.botomosy.srm1.domain.academic;

import com.botomosy.srm1.domain.tenant.Tenant;
import jakarta.persistence.*;

@Entity
@Table(
        name = "classrooms",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_classroom_tenant_name", columnNames = {"tenant_id", "name"})
        }
)
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    public Classroom() {
    }

    @PrePersist
    @PreUpdate
    public void normalize() {
        if (name != null) {
            name = name.trim();
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }
}