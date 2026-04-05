package com.botomosy.srm1.repository;

import com.botomosy.srm1.domain.payment.PaymentPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentPlanRepository extends JpaRepository<PaymentPlan, Long> {

    List<PaymentPlan> findByTenantId(Long tenantId);

    List<PaymentPlan> findByTenantIdOrderByIdDesc(Long tenantId);

    List<PaymentPlan> findByTenantIdAndActiveTrueOrderByIdDesc(Long tenantId);

    Optional<PaymentPlan> findByIdAndTenantId(Long id, Long tenantId);

    Optional<PaymentPlan> findFirstByTenantIdAndStudentIdAndActiveTrueOrderByIdDesc(Long tenantId, Long studentId);

    boolean existsByTenantIdAndStudentId(Long tenantId, Long studentId);

    boolean existsByTenantIdAndStudentIdAndActiveTrue(Long tenantId, Long studentId);
}