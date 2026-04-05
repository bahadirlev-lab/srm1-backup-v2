package com.botomosy.srm1.config;

import com.botomosy.srm1.domain.tenant.Tenant;
import com.botomosy.srm1.service.AuthService;
import com.botomosy.srm1.service.TermService;
import com.botomosy.srm1.tenant.TenantContext;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttribute {

    private final TermService termService;

    public GlobalModelAttribute(TermService termService) {
        this.termService = termService;
    }

    @ModelAttribute("sessionUsername")
    public String sessionUsername(HttpSession session) {
        Object value = session.getAttribute(AuthService.SESSION_USERNAME);
        return value != null ? value.toString() : null;
    }

    @ModelAttribute("activeTermName")
    public String activeTermName() {
        Tenant tenant = TenantContext.get();
        if (tenant == null || tenant.getId() == null) {
            return null;
        }

        return termService.getActiveTermName(tenant.getId());
    }
}