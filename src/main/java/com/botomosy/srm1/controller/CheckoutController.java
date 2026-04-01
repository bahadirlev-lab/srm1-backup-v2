package com.botomosy.srm1.controller;

import com.botomosy.srm1.service.CheckoutSessionService;
import com.botomosy.srm1.service.ProductCatalogService;
import com.botomosy.srm1.web.ProductCatalogItem;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class CheckoutController {

    private final ProductCatalogService productCatalogService;
    private final CheckoutSessionService checkoutSessionService;

    public CheckoutController(
            ProductCatalogService productCatalogService,
            CheckoutSessionService checkoutSessionService
    ) {
        this.productCatalogService = productCatalogService;
        this.checkoutSessionService = checkoutSessionService;
    }

    @GetMapping("/site/products/{productCode}/checkout")
    public String checkout(@PathVariable String productCode, Model model) {
        ProductCatalogItem product = productCatalogService.findByCode(productCode);

        if (product == null) {
            return "redirect:/site";
        }

        model.addAttribute("pageTitle", product.getName() + " Ödeme");
        model.addAttribute("product", product);

        return "site/checkout";
    }

    @PostMapping("/site/products/{productCode}/checkout")
    public String completeCheckout(@PathVariable String productCode) {
        ProductCatalogItem product = productCatalogService.findByCode(productCode);

        if (product == null) {
            return "redirect:/site";
        }

        String paymentToken = checkoutSessionService.createPaidSession(productCode);

        return "redirect:/site/products/" + productCode + "/payment-success?paymentToken=" + paymentToken;
    }

    @GetMapping("/site/products/{productCode}/payment-success")
    public String paymentSuccess(
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

        model.addAttribute("pageTitle", product.getName() + " Ödeme Başarılı");
        model.addAttribute("product", product);
        model.addAttribute("paymentToken", paymentToken);

        return "site/payment-success";
    }
}