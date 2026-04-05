package com.botomosy.srm1.repository;

import com.botomosy.srm1.domain.academic.Term;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TermRepository extends JpaRepository<Term, Long> {

    List<Term> findByTenantIdOrderByIdDesc(Long tenantId);

    Optional<Term> findByIdAndTenantId(Long id, Long tenantId);

    Optional<Term> findByTenantIdAndActiveTrue(Long tenantId);

    boolean existsByTenantIdAndNameIgnoreCase(Long tenantId, String name);
}