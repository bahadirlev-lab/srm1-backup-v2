package com.botomosy.srm1.service;

import com.botomosy.srm1.domain.tenant.Tenant;
import com.botomosy.srm1.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Locale;

@Service
public class TenantOnboardingService {

    private final TenantRepository tenantRepository;
    private final TenantPlanService tenantPlanService;

    public TenantOnboardingService(TenantRepository tenantRepository, TenantPlanService tenantPlanService) {
        this.tenantRepository = tenantRepository;
        this.tenantPlanService = tenantPlanService;
    }

    @Transactional
    public Tenant createTenant(String institutionName, String productCode) {
        String normalizedName = normalizeInstitutionName(institutionName);
        String baseSlug = toSlug(normalizedName);

        Tenant existingTenant = tenantRepository.findBySlug(baseSlug).orElse(null);
        if (existingTenant != null) {
            return existingTenant;
        }

        Tenant tenant = new Tenant();
        tenant.setName(normalizedName);
        tenant.setSlug(baseSlug);
        tenant.setActive(true);
        tenant.setProductCode(productCode);
        tenantPlanService.applyDefaultPlan(tenant);

        return tenantRepository.save(tenant);
    }

    private String normalizeInstitutionName(String institutionName) {
        if (institutionName == null) {
            throw new IllegalArgumentException("Kurum adı boş olamaz.");
        }

        String value = institutionName.trim().replaceAll("\\s+", " ");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Kurum adı boş olamaz.");
        }

        return value;
    }

    private String toSlug(String value) {
        String slug = value.toLowerCase(Locale.forLanguageTag("tr"))
                .replace("ı", "i")
                .replace("ğ", "g")
                .replace("ü", "u")
                .replace("ş", "s")
                .replace("ö", "o")
                .replace("ç", "c");

        slug = Normalizer.normalize(slug, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-{2,}", "-");

        if (slug.isBlank()) {
            slug = "kurum";
        }

        if (slug.length() > 80) {
            slug = slug.substring(0, 80).replaceAll("-+$", "");
        }

        return slug;
    }
}