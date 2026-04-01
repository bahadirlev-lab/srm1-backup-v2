package com.botomosy.srm1.controller;

import com.botomosy.srm1.domain.tenant.Tenant;
import com.botomosy.srm1.repository.TenantRepository;
import com.botomosy.srm1.tenant.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/{tenantSlug}/payments")
public class PaymentController {

    private final TenantRepository tenantRepository;

    public PaymentController(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @GetMapping
    public String list(@PathVariable String tenantSlug, Model model) {
        Tenant tenant = resolveTenantOrThrow(tenantSlug);

        model.addAttribute("tenant", tenant);
        model.addAttribute("tenantSlug", tenantSlug);
        model.addAttribute("pageTitle", "Payments");

        return "payments";
    }

    private Tenant resolveTenantOrThrow(String tenantSlug) {
        Tenant contextTenant = TenantContext.get();

        if (contextTenant != null && tenantSlug.equals(contextTenant.getSlug())) {
            return contextTenant;
        }

        return tenantRepository.findBySlugAndActiveTrue(tenantSlug)
                .orElseThrow(() -> new IllegalArgumentException("Tenant bulunamadı: " + tenantSlug));
    }
}