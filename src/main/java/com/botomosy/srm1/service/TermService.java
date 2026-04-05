package com.botomosy.srm1.service;

import com.botomosy.srm1.domain.academic.Term;
import com.botomosy.srm1.domain.tenant.Tenant;
import com.botomosy.srm1.repository.TermRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TermService {

    public static final String DEFAULT_ACTIVE_TERM_NAME = "2026-2027";

    private final TermRepository termRepository;

    public TermService(TermRepository termRepository) {
        this.termRepository = termRepository;
    }

    @Transactional
    public Term ensureDefaultActiveTerm(Tenant tenant) {
        return termRepository.findByTenantIdAndActiveTrue(tenant.getId())
                .orElseGet(() -> createDefaultTermIfMissing(tenant));
    }

    @Transactional
    public List<Term> getTerms(Tenant tenant) {
        ensureDefaultActiveTerm(tenant);
        return termRepository.findByTenantIdOrderByIdDesc(tenant.getId());
    }

    @Transactional
    public Term createTerm(Tenant tenant, String rawName) {
        String name = rawName != null ? rawName.trim() : "";

        if (name.isBlank()) {
            throw new IllegalArgumentException("Dönem adı zorunludur.");
        }

        if (termRepository.existsByTenantIdAndNameIgnoreCase(tenant.getId(), name)) {
            throw new IllegalArgumentException("Bu dönem zaten mevcut.");
        }

        Term term = new Term();
        term.setTenant(tenant);
        term.setName(name);
        term.setActive(false);

        return termRepository.save(term);
    }

    @Transactional
    public void activateTerm(Tenant tenant, Long termId) {
        Term selected = termRepository.findByIdAndTenantId(termId, tenant.getId())
                .orElseThrow(() -> new IllegalArgumentException("Dönem bulunamadı."));

        List<Term> terms = termRepository.findByTenantIdOrderByIdDesc(tenant.getId());

        for (Term term : terms) {
            term.setActive(term.getId().equals(selected.getId()));
            termRepository.save(term);
        }
    }

    @Transactional
    public void deleteTerm(Tenant tenant, Long termId) {
        Term term = termRepository.findByIdAndTenantId(termId, tenant.getId())
                .orElseThrow(() -> new IllegalArgumentException("Dönem bulunamadı."));

        if (term.isActive()) {
            throw new IllegalArgumentException("Aktif dönem silinemez.");
        }

        termRepository.delete(term);
    }

    @Transactional(readOnly = true)
    public String getActiveTermName(Long tenantId) {
        return termRepository.findByTenantIdAndActiveTrue(tenantId)
                .map(Term::getName)
                .orElse(DEFAULT_ACTIVE_TERM_NAME);
    }

    private Term createDefaultTermIfMissing(Tenant tenant) {
        List<Term> existingTerms = termRepository.findByTenantIdOrderByIdDesc(tenant.getId());

        for (Term existingTerm : existingTerms) {
            if (DEFAULT_ACTIVE_TERM_NAME.equalsIgnoreCase(existingTerm.getName())) {
                existingTerm.setActive(true);
                return termRepository.save(existingTerm);
            }
        }

        Term defaultTerm = new Term();
        defaultTerm.setTenant(tenant);
        defaultTerm.setName(DEFAULT_ACTIVE_TERM_NAME);
        defaultTerm.setActive(true);

        return termRepository.save(defaultTerm);
    }
}