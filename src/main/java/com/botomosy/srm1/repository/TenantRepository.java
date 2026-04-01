package com.botomosy.srm1.repository;

import com.botomosy.srm1.domain.tenant.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findBySlugAndActiveTrue(String slug);

    Optional<Tenant> findBySlug(String slug);

    boolean existsBySlug(String slug);
}