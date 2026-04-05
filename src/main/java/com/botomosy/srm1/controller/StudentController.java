package com.botomosy.srm1.controller;

import com.botomosy.srm1.domain.academic.Classroom;
import com.botomosy.srm1.domain.payment.Installment;
import com.botomosy.srm1.domain.payment.PaymentPlan;
import com.botomosy.srm1.domain.student.Student;
import com.botomosy.srm1.domain.tenant.Tenant;
import com.botomosy.srm1.repository.ClassroomRepository;
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
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/{tenantSlug}/students")
public class StudentController {

    private final StudentRepository studentRepository;
    private final TenantRepository tenantRepository;
    private final TenantPlanService tenantPlanService;
    private final PaymentPlanRepository paymentPlanRepository;
    private final InstallmentRepository installmentRepository;
    private final ClassroomRepository classroomRepository;

    public StudentController(
            StudentRepository studentRepository,
            TenantRepository tenantRepository,
            TenantPlanService tenantPlanService,
            PaymentPlanRepository paymentPlanRepository,
            InstallmentRepository installmentRepository,
            ClassroomRepository classroomRepository
    ) {
        this.studentRepository = studentRepository;
        this.tenantRepository = tenantRepository;
        this.tenantPlanService = tenantPlanService;
        this.paymentPlanRepository = paymentPlanRepository;
        this.installmentRepository = installmentRepository;
        this.classroomRepository = classroomRepository;
    }

    @GetMapping
    public String list(@PathVariable String tenantSlug, Model model) {
        Tenant tenant = resolveTenantOrThrow(tenantSlug);
        List<Student> students = studentRepository.findByTenantId(tenant.getId());
        long studentCount = studentRepository.countByTenantId(tenant.getId());

        model.addAttribute("tenant", tenant);
        model.addAttribute("tenantSlug", tenantSlug);
        model.addAttribute("pageTitle", "Öğrenciler");
        model.addAttribute("students", students);
        model.addAttribute("studentCount", studentCount);
        model.addAttribute("studentLimit", tenant.getStudentLimit());
        model.addAttribute("limitReached", !tenantPlanService.canAddStudent(tenant, studentCount));

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

        List<Classroom> classrooms = classroomRepository.findByTenantIdOrderByNameAsc(tenant.getId());

        model.addAttribute("tenant", tenant);
        model.addAttribute("tenantSlug", tenantSlug);
        model.addAttribute("pageTitle", "Yeni Öğrenci");
        model.addAttribute("formAction", "/" + tenantSlug + "/students");
        model.addAttribute("formTitle", "Yeni Öğrenci");
        model.addAttribute("classrooms", classrooms);

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

        String validationError = validateStudent(student, tenant);
        if (validationError != null) {
            redirectAttributes.addFlashAttribute("errorMessage", validationError);
            redirectAttributes.addFlashAttribute("student", student);
            return "redirect:/" + tenantSlug + "/students/new";
        }

        student.setId(null);
        prepareStudentForSave(student, tenant);
        student = studentRepository.save(student);

        redirectAttributes.addFlashAttribute("successMessage", "Öğrenci başarıyla eklendi.");
        return "redirect:/" + tenantSlug + "/students/" + student.getId();
    }

    @GetMapping("/{studentId}")
    public String detail(
            @PathVariable String tenantSlug,
            @PathVariable Long studentId,
            Model model
    ) {
        Tenant tenant = resolveTenantOrThrow(tenantSlug);

        Student student = studentRepository.findByIdAndTenantId(studentId, tenant.getId())
                .orElseThrow(() -> new IllegalArgumentException("Öğrenci bulunamadı: " + studentId));

        List<PaymentPlan> studentPlans = paymentPlanRepository.findByTenantIdOrderByIdDesc(tenant.getId())
                .stream()
                .filter(plan -> plan.getStudent() != null && plan.getStudent().getId().equals(student.getId()))
                .toList();

        PaymentPlan activePlan = studentPlans.stream()
                .filter(PaymentPlan::isActive)
                .findFirst()
                .orElse(studentPlans.isEmpty() ? null : studentPlans.get(0));

        List<Installment> installments = activePlan == null
                ? Collections.emptyList()
                : installmentRepository.findByPaymentPlanIdAndTenantIdOrderByInstallmentNoAsc(activePlan.getId(), tenant.getId());

        BigDecimal totalExpected = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;

        for (Installment installment : installments) {
            if (installment.getAmount() != null) {
                totalExpected = totalExpected.add(installment.getAmount());
            }
            if (installment.getPaidAmount() != null) {
                totalPaid = totalPaid.add(installment.getPaidAmount());
            }
        }

        BigDecimal remainingAmount = totalExpected.subtract(totalPaid);
        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            remainingAmount = BigDecimal.ZERO;
        }

        model.addAttribute("tenant", tenant);
        model.addAttribute("tenantSlug", tenantSlug);
        model.addAttribute("pageTitle", student.getName());
        model.addAttribute("student", student);
        model.addAttribute("activePlan", activePlan);
        model.addAttribute("studentPlans", studentPlans);
        model.addAttribute("installments", installments);
        model.addAttribute("totalExpected", totalExpected);
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("remainingAmount", remainingAmount);

        return "student-detail";
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

        List<Classroom> classrooms = classroomRepository.findByTenantIdOrderByNameAsc(tenant.getId());

        model.addAttribute("tenant", tenant);
        model.addAttribute("tenantSlug", tenantSlug);
        model.addAttribute("pageTitle", "Öğrenci Düzenle");
        model.addAttribute("formAction", "/" + tenantSlug + "/students/" + student.getId() + "/edit");
        model.addAttribute("formTitle", "Öğrenci Düzenle");
        model.addAttribute("student", student);
        model.addAttribute("classrooms", classrooms);

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

        String validationError = validateStudent(formStudent, tenant);
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
        return "redirect:/" + tenantSlug + "/students/" + existingStudent.getId();
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

        studentRepository.delete(student);

        redirectAttributes.addFlashAttribute("successMessage", "Öğrenci silindi.");
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

    private String validateStudent(Student student, Tenant tenant) {
        if (student.getFirstName() == null || student.getFirstName().trim().isBlank()) {
            return "Ad alanı zorunludur.";
        }

        if (student.getLastName() == null || student.getLastName().trim().isBlank()) {
            return "Soyad alanı zorunludur.";
        }

        if (student.getClassName() == null || student.getClassName().trim().isBlank()) {
            return "Sınıf alanı zorunludur.";
        }

        if (!classroomRepository.existsByTenantIdAndNameIgnoreCase(tenant.getId(), student.getClassName().trim())) {
            return "Geçerli bir sınıf seçmelisiniz.";
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