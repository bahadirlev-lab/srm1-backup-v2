package com.botomosy.srm1.controller;

import com.botomosy.srm1.domain.academic.Classroom;
import com.botomosy.srm1.domain.tenant.Tenant;
import com.botomosy.srm1.repository.ClassroomRepository;
import com.botomosy.srm1.repository.TenantRepository;
import com.botomosy.srm1.tenant.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/{tenantSlug}/classrooms")
public class ClassroomController {

    private final TenantRepository tenantRepository;
    private final ClassroomRepository classroomRepository;

    public ClassroomController(
            TenantRepository tenantRepository,
            ClassroomRepository classroomRepository
    ) {
        this.tenantRepository = tenantRepository;
        this.classroomRepository = classroomRepository;
    }

    @GetMapping
    public String list(@PathVariable String tenantSlug, Model model) {
        Tenant tenant = resolveTenantOrThrow(tenantSlug);
        List<Classroom> classrooms = classroomRepository.findByTenantIdOrderByNameAsc(tenant.getId());

        model.addAttribute("tenant", tenant);
        model.addAttribute("tenantSlug", tenantSlug);
        model.addAttribute("pageTitle", "Sınıflar");
        model.addAttribute("classrooms", classrooms);

        return "classrooms";
    }

    @PostMapping
    public String create(
            @PathVariable String tenantSlug,
            @RequestParam String name,
            RedirectAttributes redirectAttributes
    ) {
        Tenant tenant = resolveTenantOrThrow(tenantSlug);

        String normalizedName = name != null ? name.trim() : "";
        if (normalizedName.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Sınıf adı zorunludur.");
            return "redirect:/" + tenantSlug + "/classrooms";
        }

        if (classroomRepository.existsByTenantIdAndNameIgnoreCase(tenant.getId(), normalizedName)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bu sınıf zaten mevcut.");
            return "redirect:/" + tenantSlug + "/classrooms";
        }

        Classroom classroom = new Classroom();
        classroom.setTenant(tenant);
        classroom.setName(normalizedName);
        classroom.setActive(true);

        classroomRepository.save(classroom);

        redirectAttributes.addFlashAttribute("successMessage", "Sınıf oluşturuldu.");
        return "redirect:/" + tenantSlug + "/classrooms";
    }

    @PostMapping("/{classroomId}/delete")
    public String delete(
            @PathVariable String tenantSlug,
            @PathVariable Long classroomId,
            RedirectAttributes redirectAttributes
    ) {
        Tenant tenant = resolveTenantOrThrow(tenantSlug);

        Classroom classroom = classroomRepository.findByIdAndTenantId(classroomId, tenant.getId())
                .orElseThrow(() -> new IllegalArgumentException("Sınıf bulunamadı."));

        classroomRepository.delete(classroom);

        redirectAttributes.addFlashAttribute("successMessage", "Sınıf silindi.");
        return "redirect:/" + tenantSlug + "/classrooms";
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