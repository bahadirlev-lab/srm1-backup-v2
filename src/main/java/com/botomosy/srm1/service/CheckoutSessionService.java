package com.botomosy.srm1.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CheckoutSessionService {

    private final Map<String, CheckoutSession> sessions = new ConcurrentHashMap<>();

    public String createPaidSession(String productCode) {
        String token = UUID.randomUUID().toString();

        CheckoutSession session = new CheckoutSession(
                token,
                productCode,
                true,
                false,
                LocalDateTime.now()
        );

        sessions.put(token, session);
        return token;
    }

    public boolean isUsablePaidSession(String token, String productCode) {
        if (token == null || token.isBlank()) {
            return false;
        }

        CheckoutSession session = sessions.get(token);
        if (session == null) {
            return false;
        }

        if (!session.isPaid()) {
            return false;
        }

        if (session.isConsumed()) {
            return false;
        }

        return session.getProductCode().equals(productCode);
    }

    public void consume(String token) {
        CheckoutSession session = sessions.get(token);
        if (session != null) {
            session.setConsumed(true);
        }
    }

    private static class CheckoutSession {
        private final String token;
        private final String productCode;
        private final boolean paid;
        private boolean consumed;
        private final LocalDateTime createdAt;

        public CheckoutSession(String token, String productCode, boolean paid, boolean consumed, LocalDateTime createdAt) {
            this.token = token;
            this.productCode = productCode;
            this.paid = paid;
            this.consumed = consumed;
            this.createdAt = createdAt;
        }

        public String getToken() {
            return token;
        }

        public String getProductCode() {
            return productCode;
        }

        public boolean isPaid() {
            return paid;
        }

        public boolean isConsumed() {
            return consumed;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setConsumed(boolean consumed) {
            this.consumed = consumed;
        }
    }
}