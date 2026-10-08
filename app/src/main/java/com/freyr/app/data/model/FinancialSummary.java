package com.freyr.app.data.model;

public class FinancialSummary {
    private int pendingDebtsCount;
    private int paidDebtsCount;
    private double personalPaymentsTotal;
    private double familyDebtsPendingTotal;
    private double totalPendingAmount;
    private double totalPaidAmount;

    public FinancialSummary() {
        this.pendingDebtsCount = 0;
        this.paidDebtsCount = 0;
        this.personalPaymentsTotal = 0.0;
        this.familyDebtsPendingTotal = 0.0;
        this.totalPendingAmount = 0.0;
        this.totalPaidAmount = 0.0;
    }

    public FinancialSummary(int pendingDebtsCount, int paidDebtsCount, double personalPaymentsTotal, double familyDebtsPendingTotal, double totalPendingAmount, double totalPaidAmount) {
        this.pendingDebtsCount = pendingDebtsCount;
        this.paidDebtsCount = paidDebtsCount;
        this.personalPaymentsTotal = personalPaymentsTotal;
        this.familyDebtsPendingTotal = familyDebtsPendingTotal;
        this.totalPendingAmount = totalPendingAmount;
        this.totalPaidAmount = totalPaidAmount;
    }

    public int getPendingDebtsCount() { return pendingDebtsCount; }
    public int getPaidDebtsCount() { return paidDebtsCount; }
    public double getPersonalPaymentsTotal() { return personalPaymentsTotal; }
    public double getFamilyDebtsPendingTotal() { return familyDebtsPendingTotal; }
    public double getTotalPendingAmount() { return totalPendingAmount; }
    public double getTotalPaidAmount() { return totalPaidAmount; }
}
