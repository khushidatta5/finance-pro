package com.financepro.service;

import com.financepro.dto.ExpenseDTO;
import com.financepro.entity.Expense;
import com.financepro.repository.ExpenseRepository;
import com.financepro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/**
 * Service for Expense CRUD operations. Transactions are not a separate
 * stored collection — the {@link com.financepro.repository.TransactionRepository}
 * derives them on the fly from expenses + incomes, so edits and deletes
 * propagate to the dashboard / transactions page automatically.
 */
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public List<Expense> getAllByUser(Long userId) {
        return expenseRepository.findByUserIdOrderByExpenseDateDesc(userId);
    }

    public Expense getById(Long id, Long userId) {
        return expenseRepository.findById(id)
                .filter(e -> userId.equals(e.getUserId()))
                .orElseThrow(() -> new RuntimeException("Expense not found: " + id));
    }

    public Expense create(ExpenseDTO dto, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        Expense expense = Expense.builder()
                .title(dto.getTitle())
                .amount(normalizePositive(dto.getAmount()))
                .category(dto.getCategory().toUpperCase(Locale.ROOT))
                .expenseDate(dto.getExpenseDate())
                .notes(dto.getNotes())
                .userId(userId)
                .build();
        return expenseRepository.save(expense);
    }

    public Expense update(Long id, ExpenseDTO dto, Long userId) {
        Expense expense = getById(id, userId);
        expense.setTitle(dto.getTitle());
        expense.setAmount(normalizePositive(dto.getAmount()));
        expense.setCategory(dto.getCategory().toUpperCase(Locale.ROOT));
        expense.setExpenseDate(dto.getExpenseDate());
        expense.setNotes(dto.getNotes());
        return expenseRepository.save(expense);
    }

    public void delete(Long id, Long userId) {
        Expense expense = getById(id, userId);
        expenseRepository.delete(expense);
    }

    /** Expenses are always stored as a strictly positive amount in rupees. */
    private BigDecimal normalizePositive(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount is required");
        }
        BigDecimal abs = amount.abs();
        if (abs.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Expense amount must be greater than zero");
        }
        return abs;
    }
}
