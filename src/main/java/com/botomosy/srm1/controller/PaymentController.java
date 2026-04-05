package com.botomosy.srm1.controller;

import com.botomosy.srm1.domain.payment.Installment;
import com.botomosy.srm1.domain.payment.PaymentPlan;
import com.botomosy.srm1.domain.student.Student;
import com.botomosy.srm1.domain.tenant.Tenant;
import com.botomosy.srm1.repository.InstallmentRepository;
import com.botomosy.srm1.repository.PaymentPlanRepository;
import com.botomosy.srm1.repository.StudentRepository;
import com.botomosy.srm1.repository.TenantRepository;
import com.botomosy.srm1.tenant.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/{tenantSlug}/payments")
public class PaymentController {

    private final TenantRepository tenantRepository;
    private final StudentRepository studentRepository;
    private final PaymentPlanRepository planRepository;
    private final InstallmentRepository installmentRepository;

    public PaymentController(
            TenantRepository tenantRepository,
            StudentRepository studentRepository,
            PaymentPlanRepository planRepository,
            InstallmentRepository installmentRepository
    ) {
        this.tenantRepository = tenantRepository;
        this.studentRepository = studentRepository;
        this.planRepository = planRepository;
        this.installmentRepository = installmentRepository;
    }

    @GetMapping
    public String page(
            @PathVariable String tenantSlug,
            @RequestParam(required = false) Long planId,
            Model model
    ) {
        Tenant tenant = resolveTenantOrThrow(tenantSlug);

        List<Student> students = studentRepository.findByTenantId(tenant.getId());
        List<PaymentPlan> plans = planRepository.findByTenantIdAndActiveTrueOrderByIdDesc(tenant.getId());

        PaymentPlan selectedPlan = null;
        List<Installment> installments = Collections.emptyList();
        Student selectedStudent = null;

        if (planId != null) {
            selectedPlan = planRepository.findByIdAndTenantId(planId, tenant.getId()).orElse(null);

            if (selectedPlan != null) {
                installments = installmentRepository.findByPaymentPlanIdAndTenantIdOrderByInstallmentNoAsc(
                        selectedPlan.getId(),
                        tenant.getId()
                );
                selectedStudent = selectedPlan.getStudent();
            }
        }

        model.addAttribute("tenant", tenant);
        model.addAttribute("tenantSlug", tenantSlug);
        model.addAttribute("pageTitle", "Payments");
        model.addAttribute("students", students);
        model.addAttribute("plans", plans);
        model.addAttribute("selectedPlan", selectedPlan);
        model.addAttribute("selectedStudent", selectedStudent);
        model.addAttribute("installments", installments);

        return "payments";
    }

    @PostMapping("/create-plan")
    public String createPlan(
            @PathVariable String tenantSlug,
            @RequestParam Long studentId,
            @RequestParam BigDecimal totalAmount,
            @RequestParam int installmentCount,
            @RequestParam String startDate,
            RedirectAttributes redirectAttributes
    ) {
        Tenant tenant = resolveTenantOrThrow(tenantSlug);

        Student student = studentRepository.findById(studentId)
                .filter(s -> s.getTenant().getId().equals(tenant.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Öğrenci bulunamadı"));

        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Toplam kurs ücreti 0'dan büyük olmalı.");
            return "redirect:/" + tenantSlug + "/payments";
        }

        if (installmentCount <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Taksit sayısı 0'dan büyük olmalı.");
            return "redirect:/" + tenantSlug + "/payments";
        }

        if (startDate == null || startDate.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Başlangıç tarihi zorunludur.");
            return "redirect:/" + tenantSlug + "/payments";
        }

        if (planRepository.existsByTenantIdAndStudentIdAndActiveTrue(tenant.getId(), student.getId())) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    student.getName() + " için zaten aktif bir ödeme planı var."
            );
            return "redirect:/" + tenantSlug + "/payments";
        }

        LocalDate parsedStartDate;
        try {
            parsedStartDate = LocalDate.parse(startDate);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Başlangıç tarihi hatalı.");
            return "redirect:/" + tenantSlug + "/payments";
        }

        PaymentPlan plan = new PaymentPlan();
        plan.setTenant(tenant);
        plan.setStudent(student);
        plan.setPlanName(student.getName() + " Ödeme Planı");
        plan.setTotalAmount(totalAmount);
        plan.setInstallmentCount(installmentCount);
        plan.setStartDate(parsedStartDate);
        plan.setActive(true);

        plan = planRepository.save(plan);

        BigDecimal base = totalAmount.divide(BigDecimal.valueOf(installmentCount), 2, RoundingMode.DOWN);
        BigDecimal remainder = totalAmount.subtract(base.multiply(BigDecimal.valueOf(installmentCount)));

        for (int i = 0; i < installmentCount; i++) {
            Installment ins = new Installment();
            ins.setTenant(tenant);
            ins.setStudent(student);
            ins.setPaymentPlan(plan);
            ins.setInstallmentNo(i + 1);

            BigDecimal amount = base;
            if (i == installmentCount - 1) {
                amount = amount.add(remainder);
            }

            ins.setAmount(amount);
            ins.setDueDate(parsedStartDate.plusMonths(i));
            ins.setPaid(false);
            ins.setPaidAmount(BigDecimal.ZERO);
            ins.setPaidDate(null);

            installmentRepository.save(ins);
        }

        redirectAttributes.addFlashAttribute("successMessage", "Ödeme planı oluşturuldu.");
        return "redirect:/" + tenantSlug + "/payments?planId=" + plan.getId();
    }

    @PostMapping("/update-installment")
    public String updateInstallment(
            @PathVariable String tenantSlug,
            @RequestParam Long installmentId,
            @RequestParam BigDecimal amount,
            RedirectAttributes redirectAttributes
    ) {
        Tenant tenant = resolveTenantOrThrow(tenantSlug);
        Installment ins = findInstallmentOrThrow(installmentId, tenant.getId());

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Taksit tutarı 0'dan büyük olmalı.");
            return "redirect:/" + tenantSlug + "/payments?planId=" + ins.getPaymentPlan().getId();
        }

        ins.setAmount(amount);

        BigDecimal currentPaidAmount = ins.getPaidAmount() == null ? BigDecimal.ZERO : ins.getPaidAmount();
        if (currentPaidAmount.compareTo(amount) >= 0) {
            ins.setPaidAmount(amount);
            ins.setPaid(true);
            if (ins.getPaidDate() == null) {
                ins.setPaidDate(LocalDate.now());
            }
        } else if (currentPaidAmount.compareTo(BigDecimal.ZERO) > 0) {
            ins.setPaid(false);
        } else {
            ins.setPaid(false);
            ins.setPaidAmount(BigDecimal.ZERO);
            ins.setPaidDate(null);
        }

        installmentRepository.save(ins);
        updatePlanStatus(ins.getPaymentPlan(), tenant.getId());

        redirectAttributes.addFlashAttribute("successMessage", "Taksit tutarı güncellendi.");
        return "redirect:/" + tenantSlug + "/payments?planId=" + ins.getPaymentPlan().getId();
    }

    @PostMapping("/pay-installment")
    public String payInstallment(
            @PathVariable String tenantSlug,
            @RequestParam Long installmentId,
            RedirectAttributes redirectAttributes
    ) {
        Tenant tenant = resolveTenantOrThrow(tenantSlug);
        Installment ins = findInstallmentOrThrow(installmentId, tenant.getId());

        ins.setPaid(true);
        ins.setPaidAmount(ins.getAmount());
        ins.setPaidDate(LocalDate.now());

        installmentRepository.save(ins);
        updatePlanStatus(ins.getPaymentPlan(), tenant.getId());

        redirectAttributes.addFlashAttribute("successMessage", "Taksit ödendi.");
        return "redirect:/" + tenantSlug + "/payments?planId=" + ins.getPaymentPlan().getId();
    }

    @PostMapping("/undo-payment")
    public String undoPayment(
            @PathVariable String tenantSlug,
            @RequestParam Long installmentId,
            RedirectAttributes redirectAttributes
    ) {
        Tenant tenant = resolveTenantOrThrow(tenantSlug);
        Installment ins = findInstallmentOrThrow(installmentId, tenant.getId());

        ins.setPaid(false);
        ins.setPaidAmount(BigDecimal.ZERO);
        ins.setPaidDate(null);

        installmentRepository.save(ins);
        updatePlanStatus(ins.getPaymentPlan(), tenant.getId());

        redirectAttributes.addFlashAttribute("successMessage", "Ödeme geri alındı.");
        return "redirect:/" + tenantSlug + "/payments?planId=" + ins.getPaymentPlan().getId();
    }

    @PostMapping("/manual-payment")
    public String manualPayment(
            @PathVariable String tenantSlug,
            @RequestParam Long planId,
            @RequestParam BigDecimal amount,
            RedirectAttributes redirectAttributes
    ) {
        Tenant tenant = resolveTenantOrThrow(tenantSlug);

        PaymentPlan plan = planRepository.findByIdAndTenantId(planId, tenant.getId())
                .orElseThrow(() -> new IllegalArgumentException("Ödeme planı bulunamadı."));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Manuel ödeme tutarı 0'dan büyük olmalı.");
            return "redirect:/" + tenantSlug + "/payments?planId=" + planId;
        }

        List<Installment> installments = installmentRepository.findByPaymentPlanIdAndTenantIdOrderByInstallmentNoAsc(
                plan.getId(),
                tenant.getId()
        );

        BigDecimal remaining = amount;

        for (Installment ins : installments) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal installmentAmount = ins.getAmount();
            BigDecimal paidAmount = ins.getPaidAmount() == null ? BigDecimal.ZERO : ins.getPaidAmount();
            BigDecimal need = installmentAmount.subtract(paidAmount);

            if (need.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            if (remaining.compareTo(need) >= 0) {
                ins.setPaid(true);
                ins.setPaidAmount(installmentAmount);
                ins.setPaidDate(LocalDate.now());
                remaining = remaining.subtract(need);
            } else {
                ins.setPaid(false);
                ins.setPaidAmount(paidAmount.add(remaining));
                if (ins.getPaidDate() == null) {
                    ins.setPaidDate(LocalDate.now());
                }
                remaining = BigDecimal.ZERO;
            }

            installmentRepository.save(ins);
        }

        updatePlanStatus(plan, tenant.getId());

        redirectAttributes.addFlashAttribute("successMessage", "Manuel ödeme dağıtıldı.");
        return "redirect:/" + tenantSlug + "/payments?planId=" + plan.getId();
    }

    @PostMapping("/delete-plan")
    @Transactional
    public String deletePlan(
            @PathVariable String tenantSlug,
            @RequestParam Long planId,
            RedirectAttributes redirectAttributes
    ) {
        Tenant tenant = resolveTenantOrThrow(tenantSlug);

        PaymentPlan plan = planRepository.findByIdAndTenantId(planId, tenant.getId())
                .orElseThrow(() -> new IllegalArgumentException("Ödeme planı bulunamadı."));

        installmentRepository.deleteByPaymentPlanIdAndTenantId(plan.getId(), tenant.getId());
        planRepository.delete(plan);

        redirectAttributes.addFlashAttribute("successMessage", "Ödeme planı silindi.");
        return "redirect:/" + tenantSlug + "/payments";
    }

    private void updatePlanStatus(PaymentPlan plan, Long tenantId) {
        List<Installment> installments = installmentRepository
                .findByPaymentPlanIdAndTenantIdOrderByInstallmentNoAsc(plan.getId(), tenantId);

        boolean allPaid = !installments.isEmpty();

        for (Installment ins : installments) {
            if (!ins.isPaid()) {
                allPaid = false;
                break;
            }
        }

        plan.setActive(!allPaid);
        planRepository.save(plan);
    }

    private Installment findInstallmentOrThrow(Long installmentId, Long tenantId) {
        Installment installment = installmentRepository.findById(installmentId)
                .orElseThrow(() -> new IllegalArgumentException("Taksit bulunamadı."));

        if (installment.getTenant() == null || !tenantId.equals(installment.getTenant().getId())) {
            throw new IllegalArgumentException("Taksit bulunamadı.");
        }

        return installment;
    }

    private Tenant resolveTenantOrThrow(String tenantSlug) {
        Tenant contextTenant = TenantContext.get();

        if (contextTenant != null && tenantSlug.equals(contextTenant.getSlug())) {
            return contextTenant;
        }

        return tenantRepository.findBySlugAndActiveTrue(tenantSlug)
                .orElseThrow(() -> new IllegalArgumentException("Tenant bulunamadı"));
    }
}