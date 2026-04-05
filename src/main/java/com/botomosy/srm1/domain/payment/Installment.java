package com.botomosy.srm1.domain.payment;

import com.botomosy.srm1.domain.student.Student;
import com.botomosy.srm1.domain.tenant.Tenant;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "installments")
public class Installment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_plan_id", nullable = false)
    private PaymentPlan paymentPlan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private int installmentNo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    private boolean paid = false;

    @Column(precision = 12, scale = 2)
    private BigDecimal paidAmount;

    private LocalDate paidDate;

    public Installment() {}

    // GETTER SETTER

    public Long getId() { return id; }

    public Tenant getTenant() { return tenant; }

    public PaymentPlan getPaymentPlan() { return paymentPlan; }

    public Student getStudent() { return student; }

    public int getInstallmentNo() { return installmentNo; }

    public BigDecimal getAmount() { return amount; }

    public LocalDate getDueDate() { return dueDate; }

    public boolean isPaid() { return paid; }

    public BigDecimal getPaidAmount() { return paidAmount; }

    public LocalDate getPaidDate() { return paidDate; }

    public void setId(Long id) { this.id = id; }

    public void setTenant(Tenant tenant) { this.tenant = tenant; }

    public void setPaymentPlan(PaymentPlan paymentPlan) { this.paymentPlan = paymentPlan; }

    public void setStudent(Student student) { this.student = student; }

    public void setInstallmentNo(int installmentNo) { this.installmentNo = installmentNo; }

    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public void setPaid(boolean paid) { this.paid = paid; }

    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

    public void setPaidDate(LocalDate paidDate) { this.paidDate = paidDate; }
}