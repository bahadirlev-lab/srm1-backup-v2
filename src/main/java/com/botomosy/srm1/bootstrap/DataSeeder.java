package com.botomosy.srm1.bootstrap;

import com.botomosy.srm1.domain.tenant.Tenant;
import com.botomosy.srm1.domain.tenant.TenantPlan;
import com.botomosy.srm1.repository.TenantRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final TenantRepository tenantRepository;

    public DataSeeder(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    public void run(String... args) {
        tenantRepository.findBySlugAndActiveTrue("kurs-merkezi")
                .orElseGet(() -> {
                    Tenant tenant = new Tenant("Kurs Merkezi", "kurs-merkezi", true, "dershane");
                    tenant.setPlan(TenantPlan.BASIC);
                    tenant.setStudentLimit(100);
                    return tenantRepository.save(tenant);
                });
    }
}