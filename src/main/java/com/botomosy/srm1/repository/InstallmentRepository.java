package com.botomosy.srm1.repository;

import com.botomosy.srm1.domain.payment.Installment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface InstallmentRepository extends JpaRepository<Installment, Long> {

    List<Installment> findByTenantId(Long tenantId);

    List<Installment> findByTenantIdAndPaidFalse(Long tenantId);

    List<Installment> findByTenantIdAndDueDateBeforeAndPaidFalse(Long tenantId, LocalDate date);

    List<Installment> findByTenantIdAndPaidTrue(Long tenantId);

    List<Installment> findByPaymentPlanIdOrderByInstallmentNoAsc(Long paymentPlanId);

    List<Installment> findByPaymentPlanIdAndTenantIdOrderByInstallmentNoAsc(Long paymentPlanId, Long tenantId);

    void deleteByPaymentPlanIdAndTenantId(Long paymentPlanId, Long tenantId);
}