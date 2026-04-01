package com.botomosy.srm1.controller;

import com.botomosy.srm1.service.ProductCatalogService;
import com.botomosy.srm1.web.ProductCatalogItem;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PublicSiteController {

    private final ProductCatalogService productCatalogService;

    public PublicSiteController(ProductCatalogService productCatalogService) {
        this.productCatalogService = productCatalogService;
    }

    @GetMapping("/site")
    public String home(Model model) {
        model.addAttribute("pageTitle", "BHDR Otomasyon Sistemleri");
        model.addAttribute("products", productCatalogService.findAll());
        return "site/home";
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