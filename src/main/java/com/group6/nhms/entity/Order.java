package com.group6.nhms.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "Resident_ID", nullable = false)
    private Resident resident;

    @NotNull
    @Column(name = "IssueDate", nullable = false)
    private LocalDate issueDate;

    @Column(name = "DueDate")
    private LocalDate dueDate;

    @Size(max = 50)
    @Nationalized
    @Column(name = "PaymentStatus", length = 50)
    private String paymentStatus;

    @Column(name = "RoomPrice", precision = 18, scale = 2)
    private BigDecimal roomPrice;

    @Column(name = "ServicesPrice", precision = 18, scale = 2)
    private BigDecimal servicesPrice;

    @Column(name = "MedicationsPrice", precision = 18, scale = 2)
    private BigDecimal medicationsPrice;

    @Column(name = "TotalAmount", precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "PaymentDate")
    private LocalDateTime paymentDate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Resident getResident() {
        return resident;
    }

    public void setResident(Resident resident) {
        this.resident = resident;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public BigDecimal getRoomPrice() {
        return roomPrice;
    }

    public void setRoomPrice(BigDecimal roomPrice) {
        this.roomPrice = roomPrice;
    }

    public BigDecimal getServicesPrice() {
        return servicesPrice;
    }

    public void setServicesPrice(BigDecimal servicesPrice) {
        this.servicesPrice = servicesPrice;
    }

    public BigDecimal getMedicationsPrice() {
        return medicationsPrice;
    }

    public void setMedicationsPrice(BigDecimal medicationsPrice) {
        this.medicationsPrice = medicationsPrice;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }
}
