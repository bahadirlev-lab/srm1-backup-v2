package com.botomosy.srm1.domain.student;

import com.botomosy.srm1.domain.tenant.Tenant;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 80)
    private String firstName;

    @Column(nullable = false, length = 80)
    private String lastName;

    @Column(nullable = false, length = 80)
    private String className;

    @Column(length = 50)
    private String phone;

    @Column(length = 80)
    private String parentFirstName;

    @Column(length = 80)
    private String parentLastName;

    @Column(length = 50)
    private String parentPhone;

    @Column(length = 120)
    private String telegramChatId;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private int absenceCount = 0;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam1Correct;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam1Wrong;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam1Net;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam2Correct;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam2Wrong;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam2Net;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam3Correct;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam3Wrong;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam3Net;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam4Correct;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam4Wrong;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam4Net;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam5Correct;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam5Wrong;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam5Net;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam6Correct;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam6Wrong;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam6Net;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam7Correct;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam7Wrong;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam7Net;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam8Correct;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam8Wrong;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam8Net;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam9Correct;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam9Wrong;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam9Net;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam10Correct;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam10Wrong;

    @Column(precision = 8, scale = 2)
    private BigDecimal exam10Net;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    public Student() {
    }

    @PrePersist
    @PreUpdate
    public void syncFullName() {
        String safeFirstName = firstName != null ? firstName.trim() : "";
        String safeLastName = lastName != null ? lastName.trim() : "";
        this.firstName = safeFirstName;
        this.lastName = safeLastName;
        this.name = (safeFirstName + " " + safeLastName).trim();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        String fullName = ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
        return fullName.isBlank() ? name : fullName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getClassName() {
        return className;
    }

    public String getPhone() {
        return phone;
    }

    public String getParentFirstName() {
        return parentFirstName;
    }

    public String getParentLastName() {
        return parentLastName;
    }

    public String getParentPhone() {
        return parentPhone;
    }

    public String getTelegramChatId() {
        return telegramChatId;
    }

    public boolean isActive() {
        return active;
    }

    public int getAbsenceCount() {
        return absenceCount;
    }

    public BigDecimal getExam1Correct() {
        return exam1Correct;
    }

    public BigDecimal getExam1Wrong() {
        return exam1Wrong;
    }

    public BigDecimal getExam1Net() {
        return exam1Net;
    }

    public BigDecimal getExam2Correct() {
        return exam2Correct;
    }

    public BigDecimal getExam2Wrong() {
        return exam2Wrong;
    }

    public BigDecimal getExam2Net() {
        return exam2Net;
    }

    public BigDecimal getExam3Correct() {
        return exam3Correct;
    }

    public BigDecimal getExam3Wrong() {
        return exam3Wrong;
    }

    public BigDecimal getExam3Net() {
        return exam3Net;
    }

    public BigDecimal getExam4Correct() {
        return exam4Correct;
    }

    public BigDecimal getExam4Wrong() {
        return exam4Wrong;
    }

    public BigDecimal getExam4Net() {
        return exam4Net;
    }

    public BigDecimal getExam5Correct() {
        return exam5Correct;
    }

    public BigDecimal getExam5Wrong() {
        return exam5Wrong;
    }

    public BigDecimal getExam5Net() {
        return exam5Net;
    }

    public BigDecimal getExam6Correct() {
        return exam6Correct;
    }

    public BigDecimal getExam6Wrong() {
        return exam6Wrong;
    }

    public BigDecimal getExam6Net() {
        return exam6Net;
    }

    public BigDecimal getExam7Correct() {
        return exam7Correct;
    }

    public BigDecimal getExam7Wrong() {
        return exam7Wrong;
    }

    public BigDecimal getExam7Net() {
        return exam7Net;
    }

    public BigDecimal getExam8Correct() {
        return exam8Correct;
    }

    public BigDecimal getExam8Wrong() {
        return exam8Wrong;
    }

    public BigDecimal getExam8Net() {
        return exam8Net;
    }

    public BigDecimal getExam9Correct() {
        return exam9Correct;
    }

    public BigDecimal getExam9Wrong() {
        return exam9Wrong;
    }

    public BigDecimal getExam9Net() {
        return exam9Net;
    }

    public BigDecimal getExam10Correct() {
        return exam10Correct;
    }

    public BigDecimal getExam10Wrong() {
        return exam10Wrong;
    }

    public BigDecimal getExam10Net() {
        return exam10Net;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        syncFullName();
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        syncFullName();
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setParentFirstName(String parentFirstName) {
        this.parentFirstName = parentFirstName;
    }

    public void setParentLastName(String parentLastName) {
        this.parentLastName = parentLastName;
    }

    public void setParentPhone(String parentPhone) {
        this.parentPhone = parentPhone;
    }

    public void setTelegramChatId(String telegramChatId) {
        this.telegramChatId = telegramChatId;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setAbsenceCount(int absenceCount) {
        this.absenceCount = Math.max(absenceCount, 0);
    }

    public void setExam1Correct(BigDecimal exam1Correct) {
        this.exam1Correct = exam1Correct;
    }

    public void setExam1Wrong(BigDecimal exam1Wrong) {
        this.exam1Wrong = exam1Wrong;
    }

    public void setExam1Net(BigDecimal exam1Net) {
        this.exam1Net = exam1Net;
    }

    public void setExam2Correct(BigDecimal exam2Correct) {
        this.exam2Correct = exam2Correct;
    }

    public void setExam2Wrong(BigDecimal exam2Wrong) {
        this.exam2Wrong = exam2Wrong;
    }

    public void setExam2Net(BigDecimal exam2Net) {
        this.exam2Net = exam2Net;
    }

    public void setExam3Correct(BigDecimal exam3Correct) {
        this.exam3Correct = exam3Correct;
    }

    public void setExam3Wrong(BigDecimal exam3Wrong) {
        this.exam3Wrong = exam3Wrong;
    }

    public void setExam3Net(BigDecimal exam3Net) {
        this.exam3Net = exam3Net;
    }

    public void setExam4Correct(BigDecimal exam4Correct) {
        this.exam4Correct = exam4Correct;
    }

    public void setExam4Wrong(BigDecimal exam4Wrong) {
        this.exam4Wrong = exam4Wrong;
    }

    public void setExam4Net(BigDecimal exam4Net) {
        this.exam4Net = exam4Net;
    }

    public void setExam5Correct(BigDecimal exam5Correct) {
        this.exam5Correct = exam5Correct;
    }

    public void setExam5Wrong(BigDecimal exam5Wrong) {
        this.exam5Wrong = exam5Wrong;
    }

    public void setExam5Net(BigDecimal exam5Net) {
        this.exam5Net = exam5Net;
    }

    public void setExam6Correct(BigDecimal exam6Correct) {
        this.exam6Correct = exam6Correct;
    }

    public void setExam6Wrong(BigDecimal exam6Wrong) {
        this.exam6Wrong = exam6Wrong;
    }

    public void setExam6Net(BigDecimal exam6Net) {
        this.exam6Net = exam6Net;
    }

    public void setExam7Correct(BigDecimal exam7Correct) {
        this.exam7Correct = exam7Correct;
    }

    public void setExam7Wrong(BigDecimal exam7Wrong) {
        this.exam7Wrong = exam7Wrong;
    }

    public void setExam7Net(BigDecimal exam7Net) {
        this.exam7Net = exam7Net;
    }

    public void setExam8Correct(BigDecimal exam8Correct) {
        this.exam8Correct = exam8Correct;
    }

    public void setExam8Wrong(BigDecimal exam8Wrong) {
        this.exam8Wrong = exam8Wrong;
    }

    public void setExam8Net(BigDecimal exam8Net) {
        this.exam8Net = exam8Net;
    }

    public void setExam9Correct(BigDecimal exam9Correct) {
        this.exam9Correct = exam9Correct;
    }

    public void setExam9Wrong(BigDecimal exam9Wrong) {
        this.exam9Wrong = exam9Wrong;
    }

    public void setExam9Net(BigDecimal exam9Net) {
        this.exam9Net = exam9Net;
    }

    public void setExam10Correct(BigDecimal exam10Correct) {
        this.exam10Correct = exam10Correct;
    }

    public void setExam10Wrong(BigDecimal exam10Wrong) {
        this.exam10Wrong = exam10Wrong;
    }

    public void setExam10Net(BigDecimal exam10Net) {
        this.exam10Net = exam10Net;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public String getParentFullName() {
        String fullName = ((parentFirstName != null ? parentFirstName : "") + " " + (parentLastName != null ? parentLastName : "")).trim();
        return fullName;
    }
}