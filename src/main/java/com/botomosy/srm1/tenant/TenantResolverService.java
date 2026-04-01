package com.botomosy.srm1.tenant;

import com.botomosy.srm1.domain.tenant.Tenant;
import com.botomosy.srm1.repository.TenantRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TenantResolverService {

    private final TenantRepository tenantRepository;

    public TenantResolverService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public Optional<Tenant> resolve(HttpServletRequest request) {
        String slug = extractSlug(request);
        if (slug == null) return Optional.empty();
        return tenantRepository.findBySlugAndActiveTrue(slug);
    }

    private String extractSlug(HttpServletRequest request) {
        String host = request.getHeader("Host");

        if (host != null) {
            host = host.split(":")[0].toLowerCase();

            // kurs.localhost
            if (host.endsWith(".localhost")) {
                return host.substring(0, host.indexOf(".localhost"));
            }

            // production: tenant.domain.com
            String[] parts = host.split("\\.");
            if (parts.length >= 3) {
                return parts[0];
            }
        }

        // fallback: /kurs-merkezi
        String uri = request.getRequestURI();
        if (uri != null && uri.length() > 1) {
            return uri.split("/")[1];
        }

        return null;
    }
}