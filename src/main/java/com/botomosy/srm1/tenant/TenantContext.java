package com.botomosy.srm1.tenant;

import com.botomosy.srm1.domain.tenant.Tenant;

public final class TenantContext {

    private static final ThreadLocal<Tenant> CURRENT = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(Tenant tenant) {
        CURRENT.set(tenant);
    }

    public static Tenant get() {
        return CURRENT.get();
    }

    public static Long getId() {
        return CURRENT.get() != null ? CURRENT.get().getId() : null;
    }

    public static String getSlug() {
        return CURRENT.get() != null ? CURRENT.get().getSlug() : null;
    }

    public static void clear() {
        CURRENT.remove();
    }
}