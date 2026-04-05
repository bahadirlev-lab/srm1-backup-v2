package com.botomosy.srm1.controller;

import com.botomosy.srm1.domain.payment.Installment;
import com.botomosy.srm1.domain.payment.PaymentPlan;
import com.botomosy.srm1.domain.student.Student;
import com.botomosy.srm1.domain.tenant.Tenant;
import com.botomosy.srm1.repository.InstallmentRepository;
import com.botomosy.srm1.repository.PaymentPlanRepository;
import com.botomosy.srm1.repository.StudentRepository;
import com.botomosy.srm1.repository.TenantRepository;
import com.botomosy.srm1.service.TenantPlanService;
import com.botomosy.srm1.tenant.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/{tenantSlug}/students")
public class StudentController {

    private final StudentRepository studentRepository;
    private final TenantRepository tenantRepository;
    private final TenantPlanService tenantPlanService;
    private final PaymentPlanRepository paymentPlanRepository;
    private final InstallmentRepository installmentRepository;

    public StudentController(
            StudentRepository studentRepository,
            TenantRepository tenantRepository,
            TenantPlanService tenantPlanService,
            PaymentPlanRepository paymentPlanRepository,
            InstallmentRepository installmentRepository
    ) {
        this.studentRepository = studentRepository;
        this.tenantRepository = tenantRepository;
        this.tenantPlanService = tenantPlanService;
        this.paymentPlanRepository = paymentPlanRepository;
        this.installmentRepository = installmentRepository;
    }

    @GetMapping
    public String list(
            @PathVariable String tenantSlug,
            @RequestParam(required = false) Long studentId,
            Model model
    ) {
        Tenant tenant = resolveTenantOrThrow(tenantSlug);
        List<Student> students = studentRepository.findByTenantId(tenant.getId());
        long studentCount = studentRepository.countByTenantId(tenant.getId());

        Student selectedStudent = null;
        PaymentPlan selectedStudentPlan = null;
        List<Installment> selectedStudentInstallments = List.of();
        BigDecimal selectedStudentTotalAmount = BigDecimal.ZERO;
        BigDecimal selectedStudentTotalPaid = BigDecimal.ZERO;
        BigDecimal selectedStudentRemainingAmount = BigDecimal.ZERO;

        if (studentId != null) {
            selectedStudent = studentRepository.findByIdAndTenantId(studentId, tenant.getId()).orElse(null);

            if (selectedStudent != null) {
                Optional<PaymentPlan> optionalPlan = paymentPlanRepository
                        .findFirstByTenantIdAndStudentIdAndActiveTrueOrderByIdDesc(tenant.getId(), selectedStudent.getId());

                if (optionalPlan.isPresent()) {
                    selectedStudentPlan = optionalPlan.get();
                    selectedStudentInstallments = installmentRepository
                            .findByPaymentPlanIdAndTenantIdOrderByInstallmentNoAsc(selectedStudentPlan.getId(), tenant.getId());

                    for (Installment installment : selectedStudentInstallments) {
                        if (installment.getAmount() != null) {
                            selectedStudentTotalAmount = selectedStudentTotalAmount.add(installment.getAmount());
                        }

                        if (installment.getPaidAmount() != null) {
                            selectedStudentTotalPaid = selectedStudentTotalPaid.add(installment.getPaidAmount());
                        }
                    }

                    selectedStudentRemainingAmount = selectedStudentTotalAmount.subtract(selectedStudentTotalPaid);
                    if (selectedStudentRemainingAmount.compareTo(BigDecimal.ZERO) < 0) {
                        selectedStudentRemainingAmount = BigDecimal.ZERO;
                    }
                }
            }
        }

        model.addAttribute("tenant", tenant);
        model.addAttribute("tenantSlug", tenantSlug);
        model.addAttribute("pageTitle", "Öğrenciler");
        model.addAttribute("students", students);
        model.addAttribute("studentCount", studentCount);
        model.addAttribute("studentLimit", tenant.getStudentLimit());
        model.addAttribute("limitReached", !tenantPlanService.canAddStudent(tenant, studentCount));
        model.addAttribute("selectedStudent", selectedStudent);
        model.addAttribute("selectedStudentPlan", selectedStudentPlan);
        model.addAttribute("selectedStudentInstallments", selectedStudentInstallments);
        model.addAttribute("selectedStudentTotalAmount", selectedStudentTotalAmount);
        model.addAttribute("selectedStudentTotalPaid", selectedStudentTotalPaid);
        model.addAttribute("selectedStudentRemainingAmount", selectedStudentRemainingAmount);

        return "students";
    }

    @GetMapping("/new")
    public String createForm(@PathVariable String tenantSlug, Model model) {
        Tenant tenant = resolveTenantOrThrow(tenantSlug);
        long studentCount = studentRepository.countByTenantId(tenant.getId());

        if (!tenantPlanService.canAddStudent(tenant, studentCount)) {
            model.addAttribute("tenant", tenant);
            model.addAttribute("tenantSlug", tenantSlug);
            model.addAttribute("pageTitle", "Öğrenciler");
            model.addAttribute("students", studentRepository.findByTenantId(tenant.getId()));
            model.addAttribute("studentCount", studentCount);
            model.addAttribute("studentLimit", tenant.getStudentLimit());
            model.addAttribute("limitReached", true);
            model.addAttribute("errorMessage", tenantPlanService.buildStudentLimitMessage(tenant));
            return "students";
        }

        if (!model.containsAttribute("student")) {
            Student student = new Student();
            student.setActive(true);
            model.addAttribute("student", student);
        }

        model.addAttribute("tenant", tenant);
        model.addAttribute("tenantSlug", tenantSlug);
        model.addAttribute("pageTitle", "Yeni Öğrenci");
        model.addAttribute("formAction", "/" + tenantSlug + "/students");
        model.addAttribute("formTitle", "Yeni Öğrenci");

        return "student-form";
    }

    @PostMapping
    public String save(
            @PathVariable String tenantSlug,
            @ModelAttribute Student student,
            RedirectAttributes redirectAttributes
    ) {
        Tenant tenant = resolveTenantOrThrow(tenantSlug);
        long studentCount = studentRepository.countByTenantId(tenant.getId());

        if (!tenantPlanService.canAddStudent(tenant, studentCount)) {
            redirectAttributes.addFlashAttribute("errorMessage", tenantPlanService.buildStudentLimitMessage(tenant));
            return "redirect:/" + tenantSlug + "/students";
        }

        String validationError = validateStudent(student);
        if (validationError != null) {
            redirectAttributes.addFlashAttribute("errorMessage", validationError);
            redirectAttributes.addFlashAttribute("student", student);
            return "redirect:/" + tenantSlug + "/students/new";
        }

        student.setId(null);
        prepareStudentForSave(student, tenant);
        student = studentRepository.save(student);

        redirectAttributes.addFlashAttribute("successMessage", "Öğrenci başarıyla eklendi.");
        return "redirect:/" + tenantSlug + "/students?studentId=" + student.getId();
    }

    @GetMapping("/{studentId}/edit")
    public String editForm(
            @PathVariable String tenantSlug,
            @PathVariable Long studentId,
            Model model
    ) {
        Tenant tenant = resolveTenantOrThrow(tenantSlug);

        Student student = studentRepository.findByIdAndTenantId(studentId, tenant.getId())
                .orElseThrow(() -> new IllegalArgumentException("Öğrenci bulunamadı: " + studentId));

        model.addAttribute("tenant", tenant);
        model.addAttribute("tenantSlug", tenantSlug);
        model.addAttribute("pageTitle", "Öğrenci Düzenle");
        model.addAttribute("formAction", "/" + tenantSlug + "/students/" + student.getId() + "/edit");
        model.addAttribute("formTitle", "Öğrenci Düzenle");
        model.addAttribute("student", student);

        return "student-form";
    }

    @PostMapping("/{studentId}/edit")
    public String update(
            @PathVariable String tenantSlug,
            @PathVariable Long studentId,
            @ModelAttribute Student formStudent,
            RedirectAttributes redirectAttributes
    ) {
        Tenant tenant = resolveTenantOrThrow(tenantSlug);

        Student existingStudent = studentRepository.findByIdAndTenantId(studentId, tenant.getId())
                .orElseThrow(() -> new IllegalArgumentException("Öğrenci bulunamadı: " + studentId));

        String validationError = validateStudent(formStudent);
        if (validationError != null) {
            redirectAttributes.addFlashAttribute("errorMessage", validationError);
            return "redirect:/" + tenantSlug + "/students/" + studentId + "/edit";
        }

        existingStudent.setFirstName(formStudent.getFirstName());
        existingStudent.setLastName(formStudent.getLastName());
        existingStudent.setClassName(formStudent.getClassName());
        existingStudent.setPhone(trimToNull(formStudent.getPhone()));
        existingStudent.setParentFirstName(trimToNull(formStudent.getParentFirstName()));
        existingStudent.setParentLastName(trimToNull(formStudent.getParentLastName()));
        existingStudent.setParentPhone(trimToNull(formStudent.getParentPhone()));
        existingStudent.setTelegramChatId(trimToNull(formStudent.getTelegramChatId()));
        existingStudent.setActive(formStudent.isActive());
        existingStudent.setAbsenceCount(formStudent.getAbsenceCount());
        existingStudent.setExam1Correct(formStudent.getExam1Correct());
        existingStudent.setExam1Wrong(formStudent.getExam1Wrong());
        existingStudent.setExam1Net(formStudent.getExam1Net());
        existingStudent.setExam2Correct(formStudent.getExam2Correct());
        existingStudent.setExam2Wrong(formStudent.getExam2Wrong());
        existingStudent.setExam2Net(formStudent.getExam2Net());
        existingStudent.setExam3Correct(formStudent.getExam3Correct());
        existingStudent.setExam3Wrong(formStudent.getExam3Wrong());
        existingStudent.setExam3Net(formStudent.getExam3Net());
        existingStudent.setExam4Correct(formStudent.getExam4Correct());
        existingStudent.setExam4Wrong(formStudent.getExam4Wrong());
        existingStudent.setExam4Net(formStudent.getExam4Net());
        existingStudent.setExam5Correct(formStudent.getExam5Correct());
        existingStudent.setExam5Wrong(formStudent.getExam5Wrong());
        existingStudent.setExam5Net(formStudent.getExam5Net());
        existingStudent.setExam6Correct(formStudent.getExam6Correct());
        existingStudent.setExam6Wrong(formStudent.getExam6Wrong());
        existingStudent.setExam6Net(formStudent.getExam6Net());
        existingStudent.setExam7Correct(formStudent.getExam7Correct());
        existingStudent.setExam7Wrong(formStudent.getExam7Wrong());
        existingStudent.setExam7Net(formStudent.getExam7Net());
        existingStudent.setExam8Correct(formStudent.getExam8Correct());
        existingStudent.setExam8Wrong(formStudent.getExam8Wrong());
        existingStudent.setExam8Net(formStudent.getExam8Net());
        existingStudent.setExam9Correct(formStudent.getExam9Correct());
        existingStudent.setExam9Wrong(formStudent.getExam9Wrong());
        existingStudent.setExam9Net(formStudent.getExam9Net());
        existingStudent.setExam10Correct(formStudent.getExam10Correct());
        existingStudent.setExam10Wrong(formStudent.getExam10Wrong());
        existingStudent.setExam10Net(formStudent.getExam10Net());

        existingStudent.setTenant(tenant);
        studentRepository.save(existingStudent);

        redirectAttributes.addFlashAttribute("successMessage", "Öğrenci güncellendi.");
        return "redirect:/" + tenantSlug + "/students?studentId=" + existingStudent.getId();
    }

    @PostMapping("/{studentId}/delete")
    public String delete(
            @PathVariable String tenantSlug,
            @PathVariable Long studentId,
            RedirectAttributes redirectAttributes
    ) {
        Tenant tenant = resolveTenantOrThrow(tenantSlug);

        Student student = studentRepository.findByIdAndTenantId(studentId, tenant.getId())
                .orElseThrow(() -> new IllegalArgumentException("Öğrenci bulunamadı: " + studentId));

        if (paymentPlanRepository.existsByTenantIdAndStudentId(tenant.getId(), studentId)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    student.getName() + " silinemedi. Bu öğrenciye bağlı ödeme planı bulunduğu için önce ödeme kayıtlarını temizlemelisin."
            );
            return "redirect:/" + tenantSlug + "/students?studentId=" + studentId;
        }

        studentRepository.delete(student);

        redirectAttributes.addFlashAttribute("successMessage", "Öğrenci silindi. Limit tekrar güncellendi.");
        return "redirect:/" + tenantSlug + "/students";
    }

    private void prepareStudentForSave(Student student, Tenant tenant) {
        student.setTenant(tenant);
        student.setFirstName(student.getFirstName() != null ? student.getFirstName().trim() : null);
        student.setLastName(student.getLastName() != null ? student.getLastName().trim() : null);
        student.setClassName(student.getClassName() != null ? student.getClassName().trim() : null);
        student.setPhone(trimToNull(student.getPhone()));
        student.setParentFirstName(trimToNull(student.getParentFirstName()));
        student.setParentLastName(trimToNull(student.getParentLastName()));
        student.setParentPhone(trimToNull(student.getParentPhone()));
        student.setTelegramChatId(trimToNull(student.getTelegramChatId()));
        student.setAbsenceCount(student.getAbsenceCount());
    }

    private String validateStudent(Student student) {
        if (student.getFirstName() == null || student.getFirstName().trim().isBlank()) {
            return "Ad alanı zorunludur.";
        }

        if (student.getLastName() == null || student.getLastName().trim().isBlank()) {
            return "Soyad alanı zorunludur.";
        }

        if (student.getClassName() == null || student.getClassName().trim().isBlank()) {
            return "Sınıf alanı zorunludur.";
        }

        if (student.getAbsenceCount() < 0) {
            return "Devamsızlık bilgisi negatif olamaz.";
        }

        return null;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private Tenant resolveTenantOrThrow(String tenantSlug) {
        Tenant contextTenant = TenantContext.get();

        if (contextTenant != null && tenantSlug.equals(contextTenant.getSlug())) {
            return contextTenant;
        }

        return tenantRepository.findBySlugAndActiveTrue(tenantSlug)
                .orElseThrow(() -> new IllegalArgumentException("Tenant bulunamadı: " + tenantSlug));
    }
}