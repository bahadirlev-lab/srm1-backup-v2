package com.botomosy.srm1.controller;

import com.botomosy.srm1.domain.tenant.Tenant;
import com.botomosy.srm1.domain.user.AppUser;
import com.botomosy.srm1.repository.AppUserRepository;
import com.botomosy.srm1.repository.TenantRepository;
import com.botomosy.srm1.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/{tenantSlug}/setup")
public class SetupController {

    private final TenantRepository tenantRepository;
    private final AppUserRepository appUserRepository;
    private final AuthService authService;

    public SetupController(
            TenantRepository tenantRepository,
            AppUserRepository appUserRepository,
            AuthService authService
    ) {
        this.tenantRepository = tenantRepository;
        this.appUserRepository = appUserRepository;
        this.authService = authService;
    }

    @GetMapping
    public String setupPage(@PathVariable String tenantSlug, Model model) {

        Tenant tenant = tenantRepository.findBySlugAndActiveTrue(tenantSlug).orElse(null);
        if (tenant == null) {
            return "redirect:/site";
        }

        if (appUserRepository.findByTenantIdOrderByIdAsc(tenant.getId()).size() > 0) {
            return "redirect:/" + tenantSlug + "/login";
        }

        model.addAttribute("tenant", tenant);
        model.addAttribute("tenantSlug", tenantSlug);
        model.addAttribute("pageTitle", "Kurulum");

        return "setup";
    }

    @PostMapping
    public String createAdmin(
            @PathVariable String tenantSlug,
            @RequestParam String email,
            @RequestParam String password,
            Model model
    ) {

        Tenant tenant = tenantRepository.findBySlugAndActiveTrue(tenantSlug).orElse(null);
        if (tenant == null) {
            return "redirect:/site";
        }

        String normalizedEmail = email != null ? email.trim().toLowerCase() : "";
        String normalizedPassword = password != null ? password.trim() : "";

        if (normalizedEmail.isBlank() || normalizedPassword.isBlank()) {
            model.addAttribute("tenant", tenant);
            model.addAttribute("tenantSlug", tenantSlug);
            model.addAttribute("pageTitle", "Kurulum");
            model.addAttribute("errorMessage", "Alanlar boş olamaz");
            return "setup";
        }

        if (!normalizedEmail.contains("@")) {
            model.addAttribute("tenant", tenant);
            model.addAttribute("tenantSlug", tenantSlug);
            model.addAttribute("pageTitle", "Kurulum");
            model.addAttribute("errorMessage", "Geçerli email giriniz");
            return "setup";
        }

        if (appUserRepository.existsByTenantIdAndUsername(tenant.getId(), normalizedEmail)) {
            model.addAttribute("tenant", tenant);
            model.addAttribute("tenantSlug", tenantSlug);
            model.addAttribute("pageTitle", "Kurulum");
            model.addAttribute("errorMessage", "Bu email zaten kullanılıyor");
            return "setup";
        }

        tenant.setOwnerEmail(normalizedEmail);
        tenantRepository.save(tenant);

        AppUser user = new AppUser();
        user.setTenant(tenant);
        user.setUsername(normalizedEmail);
        user.setPasswordHash(authService.encodePassword(normalizedPassword));
        user.setRole("ADMIN");
        user.setActive(true);

        appUserRepository.save(user);

        return "redirect:/" + tenantSlug + "/login";
    }
}