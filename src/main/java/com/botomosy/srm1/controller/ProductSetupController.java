package com.botomosy.srm1.controller;

import com.botomosy.srm1.domain.tenant.Tenant;
import com.botomosy.srm1.service.CheckoutSessionService;
import com.botomosy.srm1.service.ProductCatalogService;
import com.botomosy.srm1.service.TenantOnboardingService;
import com.botomosy.srm1.web.ProductCatalogItem;
import com.botomosy.srm1.web.SetupForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ProductSetupController {

    private final ProductCatalogService productCatalogService;
    private final TenantOnboardingService tenantOnboardingService;
    private final CheckoutSessionService checkoutSessionService;

    public ProductSetupController(
            ProductCatalogService productCatalogService,
            TenantOnboardingService tenantOnboardingService,
            CheckoutSessionService checkoutSessionService
    ) {
        this.productCatalogService = productCatalogService;
        this.tenantOnboardingService = tenantOnboardingService;
        this.checkoutSessionService = checkoutSessionService;
    }

    @GetMapping("/site/products/{productCode}/setup")
    public String setup(
            @PathVariable String productCode,
            @RequestParam(required = false) String paymentToken,
            Model model
    ) {
        ProductCatalogItem product = productCatalogService.findByCode(productCode);

        if (product == null) {
            return "redirect:/site";
        }

        if (!checkoutSessionService.isUsablePaidSession(paymentToken, productCode)) {
            return "redirect:/site/products/" + productCode + "/checkout";
        }

        model.addAttribute("pageTitle", product.getName() + " Kurulum");
        model.addAttribute("product", product);
        model.addAttribute("setupForm", new SetupForm());
        model.addAttribute("paymentToken", paymentToken);

        return "site/setup";
    }

    @PostMapping("/site/products/{productCode}/setup")
    public String createTenant(
            @PathVariable String productCode,
            @RequestParam(required = false) String paymentToken,
            @ModelAttribute SetupForm setupForm,
            Model model
    ) {
        ProductCatalogItem product = productCatalogService.findByCode(productCode);

        if (product == null) {
            return "redirect:/site";
        }

        if (!checkoutSessionService.isUsablePaidSession(paymentToken, productCode)) {
            return "redirect:/site/products/" + productCode + "/checkout";
        }

        try {
            Tenant tenant = tenantOnboardingService.createTenant(setupForm.getInstitutionName(), productCode);
            checkoutSessionService.consume(paymentToken);
            return "redirect:/" + tenant.getSlug() + "/students";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("pageTitle", product.getName() + " Kurulum");
            model.addAttribute("product", product);
            model.addAttribute("setupForm", setupForm);
            model.addAttribute("paymentToken", paymentToken);
            model.addAttribute("errorMessage", ex.getMessage());
            return "site/setup";
        }
    }
}