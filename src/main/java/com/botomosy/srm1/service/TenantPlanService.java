package com.botomosy.srm1.service;

import com.botomosy.srm1.domain.tenant.Tenant;
import com.botomosy.srm1.domain.tenant.TenantPlan;
import org.springframework.stereotype.Service;

@Service
public class TenantPlanService {

    public static final int BASIC_STUDENT_LIMIT = 100;
    public static final int PRO_STUDENT_LIMIT = 1000;

    public void applyDefaultPlan(Tenant tenant) {
        tenant.setPlan(TenantPlan.BASIC);
        tenant.setStudentLimit(BASIC_STUDENT_LIMIT);
    }

    public boolean canAddStudent(Tenant tenant, long currentStudentCount) {
        return currentStudentCount < tenant.getStudentLimit();
    }

    public String buildStudentLimitMessage(Tenant tenant) {
        return "Öğrenci limiti doldu. Mevcut plan: " + tenant.getPlan() +
                ", limit: " + tenant.getStudentLimit();
    }

    public void upgradeToPro(Tenant tenant) {
        tenant.setPlan(TenantPlan.PRO);
        tenant.setStudentLimit(PRO_STUDENT_LIMIT);
    }

    public void downgradeToBasic(Tenant tenant) {
        tenant.setPlan(TenantPlan.BASIC);
        tenant.setStudentLimit(BASIC_STUDENT_LIMIT);
    }

    public boolean isPro(Tenant tenant) {
        return tenant.getPlan() == TenantPlan.PRO;
    }
}