package com.financepro.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

/**
 * DTO carrying all aggregated data needed by the dashboard view.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryDTO {

    // ── Top stat cards ──────────────────────────────────────────────────────
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal remainingBalance;
    private BigDecimal monthlyBudget;

    // ── Category breakdown ───────────────────────────────────────────────────
    private BigDecimal foodTotal;
    private BigDecimal travelTotal;
    private BigDecimal shoppingTotal;
    private BigDecimal billsTotal;

    private double foodPercentage;
    private double travelPercentage;
    private double shoppingPercentage;
    private double billsPercentage;

    // ── Budget health ────────────────────────────────────────────────────────
    /** Percentage of monthly budget consumed (0-100+). */
    private double budgetUsedPercentage;

    // ── Chart data ───────────────────────────────────────────────────────────
    /** Key = category label, value = total amount */
    private Map<String, BigDecimal> categoryData;

    /** Key = week label ("Week 1" … "Week 4"), value = total spent */
    private Map<String, BigDecimal> weeklyData;

    /** Key = month label ("Jan" … "Dec"), value = total spent */
    private Map<String, BigDecimal> monthlyData;

    // ── Recent transactions ──────────────────────────────────────────────────
    private List<TransactionDTO> recentTransactions;

    // ── Savings goals ────────────────────────────────────────────────────────
    private List<SavingsGoalDTO> savingsGoals;
}
