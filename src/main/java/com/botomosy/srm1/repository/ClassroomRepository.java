package com.botomosy.srm1.repository;

import com.botomosy.srm1.domain.academic.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    List<Classroom> findByTenantIdOrderByNameAsc(Long tenantId);

    Optional<Classroom> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByTenantIdAndNameIgnoreCase(Long tenantId, String name);
}