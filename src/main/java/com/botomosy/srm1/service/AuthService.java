package com.botomosy.srm1.service;

import com.botomosy.srm1.domain.tenant.Tenant;
import com.botomosy.srm1.domain.user.AppUser;
import com.botomosy.srm1.repository.AppUserRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class AuthService {

    public static final String SESSION_USER_ID = "SESSION_USER_ID";
    public static final String SESSION_TENANT_ID = "SESSION_TENANT_ID";
    public static final String SESSION_USERNAME = "SESSION_USERNAME";

    private final AppUserRepository appUserRepository;

    public AuthService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public AppUser authenticate(Tenant tenant, String username, String rawPassword) {
        if (tenant == null || username == null || rawPassword == null) {
            return null;
        }

        // 🔥 EMAIL NORMALIZE
        String normalized = username.trim().toLowerCase();

        AppUser user = appUserRepository
                .findByTenantIdAndUsernameAndActiveTrue(tenant.getId(), normalized)
                .orElse(null);

        if (user == null) return null;

        String hashed = hash(rawPassword);

        if (!hashed.equals(user.getPasswordHash())) {
            return null;
        }

        return user;
    }

    public String encodePassword(String rawPassword) {
        return hash(rawPassword);
    }

    private String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}