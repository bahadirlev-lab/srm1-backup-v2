package com.botomosy.srm1.controller;

import com.botomosy.srm1.domain.academic.Term;
import com.botomosy.srm1.domain.tenant.Tenant;
import com.botomosy.srm1.repository.TenantRepository;
import com.botomosy.srm1.service.TermService;
import com.botomosy.srm1.tenant.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/{tenantSlug}/terms")
public class TermController {

    private final TenantRepository tenantRepository;
    private final TermService termService;

    public TermController(
            TenantRepository tenantRepository,
            TermService termService
    ) {
        this.tenantRepository = tenantRepository;
        this.termService = termService;
    }

    @GetMapping
    public String list(@PathVariable String tenantSlug, Model model) {
        Tenant tenant = resolveTenantOrThrow(tenantSlug);
        List<Term> terms = termService.getTerms(tenant);

        model.addAttribute("tenant", tenant);
        model.addAttribute("tenantSlug", tenantSlug);
        model.addAttribute("pageTitle", "Dönemler");
        model.addAttribute("terms", terms);

        return "terms";
    }

    @PostMapping
    public String create(
            @PathVariable String tenantSlug,
            @RequestParam String name,
            RedirectAttributes redirectAttributes
    ) {
        Tenant tenant = resolveTenantOrThrow(tenantSlug);

        try {
            termService.createTerm(tenant, name);
            redirectAttributes.addFlashAttribute("successMessage", "Dönem oluşturuldu.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/" + tenantSlug + "/terms";
    }

    @PostMapping("/{termId}/activate")
    public String activate(
            @PathVariable String tenantSlug,
            @PathVariable Long termId,
            RedirectAttributes redirectAttributes
    ) {
        Tenant tenant = resolveTenantOrThrow(tenantSlug);

        try {
            termService.activateTerm(tenant, termId);
            redirectAttributes.addFlashAttribute("successMessage", "Aktif dönem güncellendi.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/" + tenantSlug + "/terms";
    }

    @PostMapping("/{termId}/delete")
    public String delete(
            @PathVariable String tenantSlug,
            @PathVariable Long termId,
            RedirectAttributes redirectAttributes
    ) {
        Tenant tenant = resolveTenantOrThrow(tenantSlug);

        try {
            termService.deleteTerm(tenant, termId);
            redirectAttributes.addFlashAttribute("successMessage", "Dönem silindi.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/" + tenantSlug + "/terms";
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