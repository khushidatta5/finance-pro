package com.financepro.repository;

import com.financepro.dto.PagedResult;
import com.financepro.entity.Expense;
import com.financepro.entity.Income;
import com.financepro.entity.Transaction;
import com.financepro.storage.JsonDataStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Transaction repository — entirely <strong>derived</strong> from the
 * {@code expenses} and {@code incomes} collections in {@code project.json}.
 *
 * Keeping transactions as a view (rather than a third stored collection)
 * means edits and deletes on an Expense or Income immediately propagate to
 * the dashboard / transactions page without any mirror-record bookkeeping.
 */
@Repository
@RequiredArgsConstructor
public class TransactionRepository {

    private final JsonDataStore store;

    // ── Reads ────────────────────────────────────────────────────────────────

    public List<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId) {
        return derive(userId).stream()
                .sorted(Comparator.comparing(Transaction::getTransactionDate).reversed())
                .collect(Collectors.toList());
    }

    public List<Transaction> findTop10ByUserIdOrderByTransactionDateDesc(Long userId) {
        return findByUserIdOrderByTransactionDateDesc(userId).stream()
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Filtered + paged listing. {@code type} / {@code category} / {@code search}
     * may be {@code null} or blank to skip that filter.
     */
    public PagedResult<Transaction> findFiltered(Long userId,
                                                 String type,
                                                 String category,
                                                 String search,
                                                 int page,
                                                 int size) {
        String typeUp = blankToUpperOrNull(type);
        String catUp  = blankToUpperOrNull(category);
        String srch   = (search == null || search.isBlank()) ? null : search.toLowerCase(Locale.ROOT);

        List<Transaction> filtered = derive(userId).stream()
                .filter(t -> typeUp == null || typeUp.equalsIgnoreCase(t.getType()))
                .filter(t -> catUp  == null || catUp.equalsIgnoreCase(t.getCategory()))
                .filter(t -> srch   == null
                        || (t.getTitle() != null && t.getTitle().toLowerCase(Locale.ROOT).contains(srch)))
                .sorted(Comparator.comparing(Transaction::getTransactionDate).reversed())
                .collect(Collectors.toList());

        return PagedResult.of(filtered, page, size);
    }

    // ── Internals ────────────────────────────────────────────────────────────

    /** Build the unified transaction view for a single user. */
    private List<Transaction> derive(Long userId) {
        return store.read(d -> {
            Stream<Transaction> expenseTx = d.getExpenses().stream()
                    .filter(e -> userId.equals(e.getUserId()))
                    .map(this::fromExpense);
            Stream<Transaction> incomeTx = d.getIncomes().stream()
                    .filter(i -> userId.equals(i.getUserId()))
                    .map(this::fromIncome);
            return Stream.concat(expenseTx, incomeTx).collect(Collectors.toList());
        });
    }

    private Transaction fromExpense(Expense e) {
        return Transaction.builder()
                .id(e.getId())
                .title(e.getTitle())
                .amount(e.getAmount())
                .type("EXPENSE")
                .category(e.getCategory())
                .transactionDate(e.getExpenseDate())
                .notes(e.getNotes())
                .userId(e.getUserId())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private Transaction fromIncome(Income i) {
        return Transaction.builder()
                .id(i.getId())
                .title(i.getTitle())
                .amount(i.getAmount())
                .type("INCOME")
                .category(i.getSource())
                .transactionDate(i.getIncomeDate())
                .notes(i.getNotes())
                .userId(i.getUserId())
                .createdAt(i.getCreatedAt())
                .build();
    }

    private static String blankToUpperOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        return s.toUpperCase(Locale.ROOT);
    }
}
