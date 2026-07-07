package com.financepro.service;

import com.financepro.dto.*;
import com.financepro.entity.SavingsGoal;
import com.financepro.entity.Transaction;
import com.financepro.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Aggregates all the data the dashboard view needs in a single trip to the
 * {@link com.financepro.storage.JsonDataStore}.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final TransactionRepository transactionRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final TransactionService transactionService;

    /** Default monthly budget — ₹50,000 feels right for an INR-first app. */
    private static final BigDecimal MONTHLY_BUDGET = new BigDecimal("50000.00");

    public DashboardSummaryDTO buildSummary(Long userId) {
        BigDecimal totalIncome   = nz(incomeRepository.sumTotalByUserId(userId));
        BigDecimal totalExpenses = nz(expenseRepository.sumTotalByUserId(userId));
        BigDecimal remaining     = totalIncome.subtract(totalExpenses);

        BigDecimal food     = nz(expenseRepository.sumByCategoryAndUserId(userId, "FOOD"));
        BigDecimal travel   = nz(expenseRepository.sumByCategoryAndUserId(userId, "TRAVEL"));
        BigDecimal shopping = nz(expenseRepository.sumByCategoryAndUserId(userId, "SHOPPING"));
        BigDecimal bills    = nz(expenseRepository.sumByCategoryAndUserId(userId, "BILLS"));

        double foodPct     = pct(food, totalExpenses);
        double travelPct   = pct(travel, totalExpenses);
        double shoppingPct = pct(shopping, totalExpenses);
        double billsPct    = pct(bills, totalExpenses);

        LocalDate now   = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);
        BigDecimal monthSpent = nz(expenseRepository.sumByUserIdAndDateRange(userId, start, now));
        double budgetUsed = pct(monthSpent, MONTHLY_BUDGET);

        Map<String, BigDecimal> categoryData = new LinkedHashMap<>();
        categoryData.put("Food", food);
        categoryData.put("Travel", travel);
        categoryData.put("Shopping", shopping);
        categoryData.put("Bills", bills);

        List<Object[]> monthlyRaw = expenseRepository.sumGroupedByMonthForYear(userId, now.getYear());
        Map<String, BigDecimal> monthlyData = buildMonthlyMap(monthlyRaw);

        Map<String, BigDecimal> weeklyData = buildWeeklyMap(userId, now);

        List<Transaction> recent = transactionRepository.findTop10ByUserIdOrderByTransactionDateDesc(userId);
        List<TransactionDTO> recentDTOs = transactionService.toDTOList(recent);

        List<SavingsGoalDTO> goals = savingsGoalRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toGoalDTO).collect(Collectors.toList());

        return DashboardSummaryDTO.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .remainingBalance(remaining)
                .monthlyBudget(MONTHLY_BUDGET)
                .foodTotal(food).travelTotal(travel)
                .shoppingTotal(shopping).billsTotal(bills)
                .foodPercentage(foodPct).travelPercentage(travelPct)
                .shoppingPercentage(shoppingPct).billsPercentage(billsPct)
                .budgetUsedPercentage(budgetUsed)
                .categoryData(categoryData)
                .weeklyData(weeklyData)
                .monthlyData(monthlyData)
                .recentTransactions(recentDTOs)
                .savingsGoals(goals)
                .build();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    private double pct(BigDecimal part, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) return 0;
        return part.multiply(BigDecimal.valueOf(100))
                   .divide(total, 1, RoundingMode.HALF_UP)
                   .doubleValue();
    }

    private Map<String, BigDecimal> buildMonthlyMap(List<Object[]> raw) {
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (String m : months) map.put(m, BigDecimal.ZERO);
        for (Object[] row : raw) {
            int monthNum = ((Number) row[0]).intValue();
            BigDecimal val = (BigDecimal) row[1];
            map.put(months[monthNum - 1], val);
        }
        return map;
    }

    private Map<String, BigDecimal> buildWeeklyMap(Long userId, LocalDate now) {
        int lastDay = now.lengthOfMonth();
        LocalDate[] starts = {
            now.withDayOfMonth(1),
            now.withDayOfMonth(Math.min(8, lastDay)),
            now.withDayOfMonth(Math.min(15, lastDay)),
            now.withDayOfMonth(Math.min(22, lastDay))
        };
        LocalDate[] ends = {
            now.withDayOfMonth(Math.min(7, lastDay)),
            now.withDayOfMonth(Math.min(14, lastDay)),
            now.withDayOfMonth(Math.min(21, lastDay)),
            now.withDayOfMonth(lastDay)
        };
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (int i = 0; i < 4; i++) {
            BigDecimal val = nz(expenseRepository.sumByUserIdAndDateRange(userId, starts[i], ends[i]));
            map.put("Week " + (i + 1), val);
        }
        return map;
    }

    private SavingsGoalDTO toGoalDTO(SavingsGoal g) {
        return SavingsGoalDTO.builder()
                .id(g.getId())
                .title(g.getTitle())
                .targetAmount(g.getTargetAmount())
                .currentAmount(g.getCurrentAmount())
                .deadline(g.getDeadline())
                .progressPercentage(g.getProgressPercentage())
                .build();
    }
}
