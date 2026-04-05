package com.botomosy.srm1.controller;

import com.botomosy.srm1.domain.tenant.Tenant;
import com.botomosy.srm1.domain.user.AppUser;
import com.botomosy.srm1.repository.TenantRepository;
import com.botomosy.srm1.service.AuthService;
import com.botomosy.srm1.service.ProductCatalogService;
import com.botomosy.srm1.web.ProductCatalogItem;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PublicSiteController {

    private final ProductCatalogService productCatalogService;
    private final TenantRepository tenantRepository;
    private final AuthService authService;

    public PublicSiteController(
            ProductCatalogService productCatalogService,
            TenantRepository tenantRepository,
            AuthService authService
    ) {
        this.productCatalogService = productCatalogService;
        this.tenantRepository = tenantRepository;
        this.authService = authService;
    }

    @GetMapping("/site")
    public String home(Model model) {
        model.addAttribute("pageTitle", "BHDR Otomasyon Sistemleri");
        model.addAttribute("products", productCatalogService.findAll());
        return "site/home";
    }

    @GetMapping("/site/login")
    public String loginLookupPage(HttpSession session, Model model) {
        Long sessionTenantId = (Long) session.getAttribute(AuthService.SESSION_TENANT_ID);
        Long sessionUserId = (Long) session.getAttribute(AuthService.SESSION_USER_ID);

        if (sessionTenantId != null && sessionUserId != null) {
            Tenant tenant = tenantRepository.findById(sessionTenantId).orElse(null);
            if (tenant != null && tenant.isActive()) {
                return "redirect:/" + tenant.getSlug() + "/students";
            }
        }

        model.addAttribute("pageTitle", "Giriş Yap");
        return "site/login-lookup";
    }

    @PostMapping("/site/login")
    public String resolveLogin(
            @RequestParam String tenantSlug,
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model
    ) {
        String normalizedTenantSlug = tenantSlug != null ? tenantSlug.trim().toLowerCase() : "";
        String normalizedEmail = email != null ? email.trim().toLowerCase() : "";
        String normalizedPassword = password != null ? password.trim() : "";

        model.addAttribute("pageTitle", "Giriş Yap");
        model.addAttribute("tenantSlug", normalizedTenantSlug);
        model.addAttribute("email", normalizedEmail);

        if (normalizedTenantSlug.isBlank()) {
            model.addAttribute("errorMessage", "Kurum kodu zorunludur.");
            return "site/login-lookup";
        }

        if (normalizedEmail.isBlank()) {
            model.addAttribute("errorMessage", "Email zorunludur.");
            return "site/login-lookup";
        }

        if (!normalizedEmail.contains("@")) {
            model.addAttribute("errorMessage", "Geçerli email giriniz.");
            return "site/login-lookup";
        }

        if (normalizedPassword.isBlank()) {
            model.addAttribute("errorMessage", "Şifre zorunludur.");
            return "site/login-lookup";
        }

        Tenant tenant = tenantRepository.findBySlugAndActiveTrue(normalizedTenantSlug).orElse(null);
        if (tenant == null) {
            model.addAttribute("errorMessage", "Kurum bulunamadı.");
            return "site/login-lookup";
        }

        AppUser authenticatedUser = authService.authenticate(tenant, normalizedEmail, normalizedPassword);
        if (authenticatedUser == null) {
            model.addAttribute("errorMessage", "Kurum kodu, email veya şifre hatalı.");
            return "site/login-lookup";
        }

        session.setAttribute(AuthService.SESSION_USER_ID, authenticatedUser.getId());
        session.setAttribute(AuthService.SESSION_TENANT_ID, tenant.getId());
        session.setAttribute(AuthService.SESSION_USERNAME, authenticatedUser.getUsername());

        return "redirect:/" + tenant.getSlug() + "/students";
    }

    @GetMapping("/site/products/{productCode}")
    public String productDetail(@PathVariable String productCode, Model model) {
        ProductCatalogItem product = productCatalogService.findByCode(productCode);

        if (product == null) {
            return "redirect:/site";
        }

        model.addAttribute("pageTitle", product.getName());
        model.addAttribute("product", product);
        return "site/product-detail";
    }
}