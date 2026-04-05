package com.botomosy.srm1.controller;

import com.botomosy.srm1.domain.payment.Installment;
import com.botomosy.srm1.domain.student.Student;
import com.botomosy.srm1.domain.tenant.Tenant;
import com.botomosy.srm1.repository.InstallmentRepository;
import com.botomosy.srm1.repository.StudentRepository;
import com.botomosy.srm1.repository.TenantRepository;
import com.botomosy.srm1.tenant.TenantContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final TenantRepository tenantRepository;
    private final StudentRepository studentRepository;
    private final InstallmentRepository installmentRepository;

    public DashboardController(
            TenantRepository tenantRepository,
            StudentRepository studentRepository,
            InstallmentRepository installmentRepository
    ) {
        this.tenantRepository = tenantRepository;
        this.studentRepository = studentRepository;
        this.installmentRepository = installmentRepository;
    }

    @GetMapping("/{tenantSlug}")
    public String dashboard(@PathVariable String tenantSlug, Model model) {

        Tenant tenant = tenantRepository.findBySlugAndActiveTrue(tenantSlug).orElse(null);
        if (tenant == null) {
            return "redirect:/site";
        }

        TenantContext.set(tenant);

        Long tenantId = tenant.getId();

        // 🔥 öğrenci
        List<Student> students = studentRepository.findByTenantId(tenantId);
        long totalStudents = students.size();

        // 🔥 tüm taksitler
        List<Installment> all = installmentRepository.findByTenantId(tenantId);

        BigDecimal totalExpected = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;

        BigDecimal monthExpected = BigDecimal.ZERO;
        BigDecimal monthPaid = BigDecimal.ZERO;

        LocalDate now = LocalDate.now();

        for (Installment i : all) {

            totalExpected = totalExpected.add(i.getAmount());

            if (i.isPaid() && i.getPaidAmount() != null) {
                totalPaid = totalPaid.add(i.getPaidAmount());
            }

            if (i.getDueDate().getMonth() == now.getMonth()
                    && i.getDueDate().getYear() == now.getYear()) {

                monthExpected = monthExpected.add(i.getAmount());

                if (i.isPaid() && i.getPaidAmount() != null) {
                    monthPaid = monthPaid.add(i.getPaidAmount());
                }
            }
        }

        // 🔥 gecikenler
        List<Installment> overdue = installmentRepository
                .findByTenantIdAndDueDateBeforeAndPaidFalse(tenantId, now);

        // 🔥 SON 6 AY TAHSİLAT
        Map<String, BigDecimal> last6Months = new LinkedHashMap<>();

        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);

            BigDecimal sum = BigDecimal.ZERO;

            for (Installment ins : all) {
                if (ins.isPaid() && ins.getPaidDate() != null) {

                    YearMonth paidYm = YearMonth.from(ins.getPaidDate());

                    if (paidYm.equals(ym) && ins.getPaidAmount() != null) {
                        sum = sum.add(ins.getPaidAmount());
                    }
                }
            }

            last6Months.put(ym.getMonth().toString(), sum);
        }

        model.addAttribute("tenant", tenant);
        model.addAttribute("tenantSlug", tenantSlug);

        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("studentLimit", tenant.getStudentLimit());

        model.addAttribute("totalExpected", totalExpected);
        model.addAttribute("totalPaid", totalPaid);

        model.addAttribute("monthExpected", monthExpected);
        model.addAttribute("monthPaid", monthPaid);

        model.addAttribute("overdueList", overdue);

        model.addAttribute("ownerEmail", tenant.getOwnerEmail());

        model.addAttribute("chartLabels", last6Months.keySet());
        model.addAttribute("chartData", last6Months.values());

        return "dashboard";
    }
}