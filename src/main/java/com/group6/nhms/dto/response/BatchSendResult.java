package com.group6.nhms.dto.response;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class BatchSendResult {
    private int totalEligible;
    private int successCount;
    private int failedCount;
    private int skippedCount;
    private List<String> recipientEmails = new ArrayList<>();
    private BigDecimal totalUnpaidReminded = BigDecimal.ZERO;
    private int totalIncidentsReported = 0;


    public BatchSendResult() {
    }


    public int getTotalEligible() { return totalEligible; }
    public void setTotalEligible(int totalEligible) { this.totalEligible = totalEligible; }


    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }


    public int getFailedCount() { return failedCount; }
    public void setFailedCount(int failedCount) { this.failedCount = failedCount; }


    public int getSkippedCount() { return skippedCount; }
    public void setSkippedCount(int skippedCount) { this.skippedCount = skippedCount; }


    public List<String> getRecipientEmails() { return recipientEmails; }
    public void setRecipientEmails(List<String> recipientEmails) { this.recipientEmails = recipientEmails; }


    public BigDecimal getTotalUnpaidReminded() { return totalUnpaidReminded; }
    public void setTotalUnpaidReminded(BigDecimal totalUnpaidReminded) { this.totalUnpaidReminded = totalUnpaidReminded; }


    public int getTotalIncidentsReported() { return totalIncidentsReported; }
    public void setTotalIncidentsReported(int totalIncidentsReported) { this.totalIncidentsReported = totalIncidentsReported; }
}

