package com.botomosy.srm1.config;

import com.botomosy.srm1.tenant.AuthInterceptor;
import com.botomosy.srm1.tenant.TenantInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final TenantInterceptor tenantInterceptor;
    private final AuthInterceptor authInterceptor;

    public WebConfig(TenantInterceptor tenantInterceptor, AuthInterceptor authInterceptor) {
        this.tenantInterceptor = tenantInterceptor;
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // Tenant çözümleme (HER ŞEYDEN ÖNCE)
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/h2-console/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/error",
                        "/favicon.ico"
                );

        // Auth kontrol (login hariç her şey)
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/site/**",
                        "/h2-console/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/error",
                        "/favicon.ico",
                        "/*/login",     // 🔥 KRİTİK
                        "/*/logout"     // 🔥 KRİTİK
                );
    }
}