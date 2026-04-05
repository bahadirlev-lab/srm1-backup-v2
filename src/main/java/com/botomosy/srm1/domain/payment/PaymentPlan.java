package com.botomosy.srm1.domain.payment;

import com.botomosy.srm1.domain.student.Student;
import com.botomosy.srm1.domain.tenant.Tenant;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payment_plans")
public class PaymentPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false, length = 150)
    private String planName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private int installmentCount;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private boolean active = true;

    public PaymentPlan() {}

    // GETTER SETTER

    public Long getId() { return id; }

    public Tenant getTenant() { return tenant; }

    public Student getStudent() { return student; }

    public String getPlanName() { return planName; }

    public BigDecimal getTotalAmount() { return totalAmount; }

    public int getInstallmentCount() { return installmentCount; }

    public LocalDate getStartDate() { return startDate; }

    public boolean isActive() { return active; }

    public void setId(Long id) { this.id = id; }

    public void setTenant(Tenant tenant) { this.tenant = tenant; }

    public void setStudent(Student student) { this.student = student; }

    public void setPlanName(String planName) { this.planName = planName; }

    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public void setInstallmentCount(int installmentCount) { this.installmentCount = installmentCount; }

    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public void setActive(boolean active) { this.active = active; }
}