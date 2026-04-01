package com.botomosy.srm1.service;

import com.botomosy.srm1.web.ProductCatalogItem;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductCatalogService {

    public List<ProductCatalogItem> findAll() {
        return List.of(
                new ProductCatalogItem(
                        "dershane",
                        "Dershane Otomasyonu",
                        "Öğrenci, ödeme, sınıf ve dönem yönetimi",
                        "Dershaneler ve kurs merkezleri için öğrenci kayıt, taksit planı, sınıf ve dönem takibi sunar.",
                        "999,00 TL"
                ),
                new ProductCatalogItem(
                        "klinik",
                        "Klinik Otomasyonu",
                        "Danışan, randevu ve ödeme yönetimi",
                        "Klinikler için hasta kaydı, randevu, tahsilat ve temel operasyon yönetimi sunar.",
                        "999,00 TL"
                ),
                new ProductCatalogItem(
                        "surucu-kursu",
                        "Sürücü Kursu Otomasyonu",
                        "Kursiyer, ödeme ve sınıf süreçleri",
                        "Sürücü kursları için kursiyer yönetimi, ödeme takibi ve operasyon süreçlerini yönetir.",
                        "999,00 TL"
                )
        );
    }

    public ProductCatalogItem findByCode(String code) {
        return findAll().stream()
                .filter(item -> item.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}