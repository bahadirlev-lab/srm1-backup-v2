package com.botomosy.srm1.repository;

import com.botomosy.srm1.domain.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByTenantIdAndUsernameAndActiveTrue(Long tenantId, String username);

    List<AppUser> findAllByUsernameAndActiveTrue(String username);

    boolean existsByTenantIdAndUsername(Long tenantId, String username);

    List<AppUser> findByTenantIdOrderByIdAsc(Long tenantId);

    Optional<AppUser> findByIdAndTenantId(Long id, Long tenantId);
}