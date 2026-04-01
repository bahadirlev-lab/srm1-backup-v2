package com.botomosy.srm1.controller;

import com.botomosy.srm1.domain.tenant.Tenant;
import com.botomosy.srm1.repository.TenantRepository;
import com.botomosy.srm1.tenant.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class DashboardController {

    private final TenantRepository tenantRepository;

    public DashboardController(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @GetMapping("/")
    public String root(Model model) {
        Tenant tenant = TenantContext.get();

        if (tenant == null) {
            return "redirect:/site";
        }

        model.addAttribute("tenant", tenant);
        model.addAttribute("tenantSlug", tenant.getSlug());
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("message", "Tenant başarıyla çözüldü");
        return "dashboard";
    }

    @GetMapping("/{tenantSlug}")
    public String tenantDashboard(@PathVariable String tenantSlug, Model model) {
        Tenant tenant = tenantRepository.findBySlugAndActiveTrue(tenantSlug).orElse(null);

        if (tenant == null) {
            return "redirect:/site";
        }

        TenantContext.set(tenant);
        model.addAttribute("tenant", tenant);
        model.addAttribute("tenantSlug", tenant.getSlug());
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("message", "Tenant path üzerinden başarıyla çözüldü");
        return "dashboard";
    }
}