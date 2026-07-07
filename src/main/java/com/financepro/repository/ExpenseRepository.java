package com.financepro.repository;

import com.financepro.entity.Expense;
import com.financepro.storage.JsonDataStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JSON-backed repository for {@link Expense}. Mirrors the surface the
 * services previously used against Spring Data JPA.
 */
@Repository
@RequiredArgsConstructor
public class ExpenseRepository {

    private final JsonDataStore store;

    // ── Basic CRUD ───────────────────────────────────────────────────────────

    public Optional<Expense> findById(Long id) {
        return store.read(d -> d.getExpenses().stream()
                .filter(e -> id.equals(e.getId()))
                .findFirst());
    }

    public Expense save(Expense expense) {
        return store.mutate(d -> {
            LocalDateTime now = LocalDateTime.now();
            if (expense.getId() == null) {
                expense.setId(store.nextId("expenses"));
                expense.setCreatedAt(now);
                expense.setUpdatedAt(now);
                d.getExpenses().add(expense);
            } else {
                expense.setUpdatedAt(now);
                d.getExpenses().removeIf(e -> e.getId().equals(expense.getId()));
                d.getExpenses().add(expense);
            }
            return expense;
        });
    }

    public void delete(Expense expense) {
        store.mutate(d -> {
            d.getExpenses().removeIf(e -> e.getId().equals(expense.getId()));
            return null;
        });
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    public List<Expense> findByUserIdOrderByExpenseDateDesc(Long userId) {
        return store.read(d -> d.getExpenses().stream()
                .filter(e -> userId.equals(e.getUserId()))
                .sorted(Comparator.comparing(Expense::getExpenseDate).reversed())
                .collect(Collectors.toList()));
    }

    public BigDecimal sumTotalByUserId(Long userId) {
        return store.read(d -> d.getExpenses().stream()
                .filter(e -> userId.equals(e.getUserId()))
                .map(Expense::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public BigDecimal sumByCategoryAndUserId(Long userId, String category) {
        String cat = category == null ? null : category.toUpperCase(Locale.ROOT);
        return store.read(d -> d.getExpenses().stream()
                .filter(e -> userId.equals(e.getUserId()))
                .filter(e -> cat == null || cat.equalsIgnoreCase(e.getCategory()))
                .map(Expense::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public BigDecimal sumByUserIdAndDateRange(Long userId, LocalDate start, LocalDate end) {
        return store.read(d -> d.getExpenses().stream()
                .filter(e -> userId.equals(e.getUserId()))
                .filter(e -> e.getExpenseDate() != null
                        && !e.getExpenseDate().isBefore(start)
                        && !e.getExpenseDate().isAfter(end))
                .map(Expense::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    /** Returns {@code [monthNumber (1..12), totalAmount]} pairs for the year. */
    public List<Object[]> sumGroupedByMonthForYear(Long userId, int year) {
        return store.read(d -> {
            Map<Integer, BigDecimal> byMonth = new TreeMap<>();
            for (Expense e : d.getExpenses()) {
                if (!userId.equals(e.getUserId()) || e.getExpenseDate() == null) continue;
                if (e.getExpenseDate().getYear() != year) continue;
                byMonth.merge(e.getExpenseDate().getMonthValue(),
                              e.getAmount() == null ? BigDecimal.ZERO : e.getAmount(),
                              BigDecimal::add);
            }
            List<Object[]> rows = new ArrayList<>();
            byMonth.forEach((m, v) -> rows.add(new Object[]{m, v}));
            return rows;
        });
    }
}
