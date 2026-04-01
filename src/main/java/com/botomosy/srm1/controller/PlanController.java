package com.botomosy.srm1.controller;

import com.botomosy.srm1.domain.tenant.Tenant;
import com.botomosy.srm1.repository.StudentRepository;
import com.botomosy.srm1.repository.TenantRepository;
import com.botomosy.srm1.service.TenantPlanService;
import com.botomosy.srm1.tenant.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/{tenantSlug}/plans")
public class PlanController {

    private final TenantRepository tenantRepository;
    private final StudentRepository studentRepository;
    private final TenantPlanService tenantPlanService;

    public PlanController(
            TenantRepository tenantRepository,
            StudentRepository studentRepository,
            TenantPlanService tenantPlanService
    ) {
        this.tenantRepository = tenantRepository;
        this.studentRepository = studentRepository;
        this.tenantPlanService = tenantPlanService;
    }

    @GetMapping
    public String page(@PathVariable String tenantSlug, Model model) {
        Tenant tenant = resolveTenantOrThrow(tenantSlug);
        long studentCount = studentRepository.countByTenantId(tenant.getId());

        model.addAttribute("tenant", tenant);
        model.addAttribute("tenantSlug", tenantSlug);
        model.addAttribute("pageTitle", "Planlar");
        model.addAttribute("studentCount", studentCount);
        model.addAttribute("basicLimit", TenantPlanService.BASIC_STUDENT_LIMIT);
        model.addAttribute("proLimit", TenantPlanService.PRO_STUDENT_LIMIT);
        model.addAttribute("isPro", tenantPlanService.isPro(tenant));

        return "plans";
    }

    @PostMapping("/upgrade")
    public String upgradeToPro(@PathVariable String tenantSlug) {
        Tenant tenant = resolveTenantOrThrow(tenantSlug);

        tenantPlanService.upgradeToPro(tenant);
        tenantRepository.save(tenant);

        return "redirect:/" + tenantSlug + "/plans";
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