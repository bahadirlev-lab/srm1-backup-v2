package com.botomosy.srm1.repository;

import com.botomosy.srm1.domain.student.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findByTenantId(Long tenantId);

    long countByTenantId(Long tenantId);

    Optional<Student> findByIdAndTenantId(Long id, Long tenantId);
}