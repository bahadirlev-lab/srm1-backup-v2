package com.botomosy.srm1.controller;

import com.botomosy.srm1.domain.tenant.Tenant;
import com.botomosy.srm1.domain.user.AppUser;
import com.botomosy.srm1.repository.TenantRepository;
import com.botomosy.srm1.service.AuthService;
import com.botomosy.srm1.web.LoginForm;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/{tenantSlug}")
public class AuthController {

    private final TenantRepository tenantRepository;
    private final AuthService authService;

    public AuthController(TenantRepository tenantRepository, AuthService authService) {
        this.tenantRepository = tenantRepository;
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage(
            @PathVariable String tenantSlug,
            @RequestParam(required = false) String username,
            Model model,
            HttpSession session
    ) {
        Tenant tenant = tenantRepository.findBySlugAndActiveTrue(tenantSlug).orElse(null);
        if (tenant == null) {
            return "redirect:/site";
        }

        Long sessionTenantId = (Long) session.getAttribute(AuthService.SESSION_TENANT_ID);
        Long sessionUserId = (Long) session.getAttribute(AuthService.SESSION_USER_ID);

        if (sessionTenantId != null && sessionUserId != null && tenant.getId().equals(sessionTenantId)) {
            return "redirect:/" + tenantSlug + "/students";
        }

        model.addAttribute("tenant", tenant);
        model.addAttribute("tenantSlug", tenantSlug);
        model.addAttribute("pageTitle", "Giriş Yap");

        if (!model.containsAttribute("loginForm")) {
            LoginForm loginForm = new LoginForm();
            if (username != null && !username.trim().isBlank()) {
                loginForm.setUsername(username.trim().toLowerCase());
            }
            model.addAttribute("loginForm", loginForm);
        }

        return "login";
    }

    @PostMapping("/login")
    public String login(
            @PathVariable String tenantSlug,
            @ModelAttribute LoginForm loginForm,
            Model model,
            HttpSession session
    ) {
        Tenant tenant = tenantRepository.findBySlugAndActiveTrue(tenantSlug).orElse(null);
        if (tenant == null) {
            return "redirect:/site";
        }

        AppUser user = authService.authenticate(tenant, loginForm.getUsername(), loginForm.getPassword());

        if (user == null) {
            model.addAttribute("tenant", tenant);
            model.addAttribute("tenantSlug", tenantSlug);
            model.addAttribute("pageTitle", "Giriş Yap");
            model.addAttribute("loginForm", loginForm);
            model.addAttribute("errorMessage", "Kullanıcı adı veya şifre hatalı.");
            return "login";
        }

        session.setAttribute(AuthService.SESSION_USER_ID, user.getId());
        session.setAttribute(AuthService.SESSION_TENANT_ID, tenant.getId());
        session.setAttribute(AuthService.SESSION_USERNAME, user.getUsername());

        return "redirect:/" + tenantSlug + "/students";
    }

    @PostMapping("/logout")
    public String logout(@PathVariable String tenantSlug, HttpSession session) {
        session.invalidate();
        return "redirect:/" + tenantSlug + "/login";
    }
}