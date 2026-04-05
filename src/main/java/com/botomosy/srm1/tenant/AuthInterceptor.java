package com.botomosy.srm1.tenant;

import com.botomosy.srm1.domain.tenant.Tenant;
import com.botomosy.srm1.repository.AppUserRepository;
import com.botomosy.srm1.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AppUserRepository appUserRepository;

    public AuthInterceptor(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();

        if (uri == null || uri.isBlank()) {
            return true;
        }

        String normalized = uri.startsWith("/") ? uri.substring(1) : uri;
        if (normalized.isBlank()) {
            return true;
        }

        String[] parts = normalized.split("/");
        if (parts.length == 0) {
            return true;
        }

        String tenantSlug = parts[0];

        if (tenantSlug.equals("site")
                || tenantSlug.equals("css")
                || tenantSlug.equals("js")
                || tenantSlug.equals("images")
                || tenantSlug.equals("h2-console")
                || tenantSlug.equals("error")
                || tenantSlug.equals("favicon.ico")) {
            return true;
        }

        Tenant tenant = TenantContext.get();
        if (tenant == null) {
            response.sendRedirect("/site");
            return false;
        }

        if (parts.length >= 2) {
            String second = parts[1];

            if (second.equals("login") || second.equals("logout")) {
                return true;
            }

            if (second.equals("setup")) {
                boolean hasAnyUser = !appUserRepository.findByTenantIdOrderByIdAsc(tenant.getId()).isEmpty();

                if (!hasAnyUser) {
                    return true;
                }

                response.sendRedirect("/" + tenantSlug + "/login");
                return false;
            }
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("/" + tenantSlug + "/login");
            return false;
        }

        Long tenantId = (Long) session.getAttribute(AuthService.SESSION_TENANT_ID);
        Long userId = (Long) session.getAttribute(AuthService.SESSION_USER_ID);

        if (tenantId == null || userId == null || !tenant.getId().equals(tenantId)) {
            response.sendRedirect("/" + tenantSlug + "/login");
            return false;
        }

        boolean userExistsForTenant = appUserRepository.findByIdAndTenantId(userId, tenant.getId()).isPresent();
        if (!userExistsForTenant) {
            session.invalidate();
            response.sendRedirect("/" + tenantSlug + "/login");
            return false;
        }

        return true;
    }
}