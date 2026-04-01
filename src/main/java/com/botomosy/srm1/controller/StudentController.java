package com.botomosy.srm1.controller;

import com.botomosy.srm1.domain.student.Student;
import com.botomosy.srm1.domain.tenant.Tenant;
import com.botomosy.srm1.repository.StudentRepository;
import com.botomosy.srm1.repository.TenantRepository;
import com.botomosy.srm1.service.TenantPlanService;
import com.botomosy.srm1.tenant.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/{tenantSlug}/students")
public class StudentController {

    private final StudentRepository studentRepository;
    private final TenantRepository tenantRepository;
    private final TenantPlanService tenantPlanService;

    public StudentController(
            StudentRepository studentRepository,
            TenantRepository tenantRepository,
            TenantPlanService tenantPlanService
    ) {
        this.studentRepository = studentRepository;
        this.tenantRepository = tenantRepository;
        this.tenantPlanService = tenantPlanService;
    }

    @GetMapping
    public String list(@PathVariable String tenantSlug, Model model) {
        Tenant tenant = resolveTenantOrThrow(tenantSlug);
        List<Student> students = studentRepository.findByTenantId(tenant.getId());
        long studentCount = studentRepository.countByTenantId(tenant.getId());

        model.addAttribute("tenant", tenant);
        model.addAttribute("tenantSlug", tenantSlug);
        model.addAttribute("pageTitle", "Students");
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
            model.addAttribute("pageTitle", "Students");
            model.addAttribute("students", studentRepository.findByTenantId(tenant.getId()));
            model.addAttribute("studentCount", studentCount);
            model.addAttribute("studentLimit", tenant.getStudentLimit());
            model.addAttribute("limitReached", true);
            model.addAttribute("errorMessage", tenantPlanService.buildStudentLimitMessage(tenant));
            return "students";
        }

        if (!model.containsAttribute("student")) {
            model.addAttribute("student", new Student());
        }

        model.addAttribute("tenant", tenant);
        model.addAttribute("tenantSlug", tenantSlug);
        model.addAttribute("pageTitle", "Yeni Student");

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

        if (student.getName() == null || student.getName().trim().isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Öğrenci adı boş olamaz.");
            redirectAttributes.addFlashAttribute("student", student);
            return "redirect:/" + tenantSlug + "/students/new";
        }

        student.setId(null);
        student.setTenant(tenant);
        student.setName(student.getName().trim());

        if (student.getPhone() != null) {
            student.setPhone(student.getPhone().trim());
        }

        studentRepository.save(student);

        redirectAttributes.addFlashAttribute("successMessage", "Öğrenci başarıyla eklendi.");
        return "redirect:/" + tenantSlug + "/students";
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

        redirectAttributes.addFlashAttribute("successMessage", "Öğrenci silindi. Limit tekrar güncellendi.");
        return "redirect:/" + tenantSlug + "/students";
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