package com.group6.nhms.dto.response;

import java.math.BigDecimal;

/**
 * Thống kê doanh thu và công nợ viện phí tổng hợp theo từng tháng.
 */
public class MonthlyRevenueSummary {

    private int year;
    private int month;
    private long totalOrders;
    private long paidOrders;
    private long unpaidOrders;
    private BigDecimal totalRoomRevenue;
    private BigDecimal totalServiceAndMedRevenue;
    private BigDecimal collectedRevenue;
    private BigDecimal pendingRevenue;
    private BigDecimal totalBilledAmount;

    public MonthlyRevenueSummary() {
        this.totalRoomRevenue = BigDecimal.ZERO;
        this.totalServiceAndMedRevenue = BigDecimal.ZERO;
        this.collectedRevenue = BigDecimal.ZERO;
        this.pendingRevenue = BigDecimal.ZERO;
        this.totalBilledAmount = BigDecimal.ZERO;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public String getPeriodLabel() {
        return String.format("%02d/%d", month, year);
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getPaidOrders() {
        return paidOrders;
    }

    public void setPaidOrders(long paidOrders) {
        this.paidOrders = paidOrders;
    }

    public long getUnpaidOrders() {
        return unpaidOrders;
    }

    public void setUnpaidOrders(long unpaidOrders) {
        this.unpaidOrders = unpaidOrders;
    }

    public BigDecimal getTotalRoomRevenue() {
        return totalRoomRevenue;
    }

    public void setTotalRoomRevenue(BigDecimal totalRoomRevenue) {
        this.totalRoomRevenue = totalRoomRevenue;
    }

    public BigDecimal getTotalServiceAndMedRevenue() {
        return totalServiceAndMedRevenue;
    }

    public void setTotalServiceAndMedRevenue(BigDecimal totalServiceAndMedRevenue) {
        this.totalServiceAndMedRevenue = totalServiceAndMedRevenue;
    }

    public BigDecimal getCollectedRevenue() {
        return collectedRevenue;
    }

    public void setCollectedRevenue(BigDecimal collectedRevenue) {
        this.collectedRevenue = collectedRevenue;
    }

    public BigDecimal getPendingRevenue() {
        return pendingRevenue;
    }

    public void setPendingRevenue(BigDecimal pendingRevenue) {
        this.pendingRevenue = pendingRevenue;
    }

    public BigDecimal getTotalBilledAmount() {
        return totalBilledAmount;
    }

    public void setTotalBilledAmount(BigDecimal totalBilledAmount) {
        this.totalBilledAmount = totalBilledAmount;
    }
}
