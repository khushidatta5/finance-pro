package com.financepro.service;

import com.financepro.dto.IncomeDTO;
import com.financepro.entity.Income;
import com.financepro.repository.IncomeRepository;
import com.financepro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/**
 * Service for Income CRUD operations. Transactions are derived from
 * incomes + expenses on read, so no mirroring is needed here.
 */
@Service
@RequiredArgsConstructor
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final UserRepository userRepository;

    public List<Income> getAllByUser(Long userId) {
        return incomeRepository.findByUserIdOrderByIncomeDateDesc(userId);
    }

    public Income getById(Long id, Long userId) {
        return incomeRepository.findById(id)
                .filter(i -> userId.equals(i.getUserId()))
                .orElseThrow(() -> new RuntimeException("Income record not found: " + id));
    }

    public Income create(IncomeDTO dto, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        Income income = Income.builder()
                .title(dto.getTitle())
                .amount(normalizePositive(dto.getAmount()))
                .source(dto.getSource().toUpperCase(Locale.ROOT))
                .incomeDate(dto.getIncomeDate())
                .notes(dto.getNotes())
                .userId(userId)
                .build();
        return incomeRepository.save(income);
    }

    public Income update(Long id, IncomeDTO dto, Long userId) {
        Income income = getById(id, userId);
        income.setTitle(dto.getTitle());
        income.setAmount(normalizePositive(dto.getAmount()));
        income.setSource(dto.getSource().toUpperCase(Locale.ROOT));
        income.setIncomeDate(dto.getIncomeDate());
        income.setNotes(dto.getNotes());
        return incomeRepository.save(income);
    }

    public void delete(Long id, Long userId) {
        Income income = getById(id, userId);
        incomeRepository.delete(income);
    }

    private BigDecimal normalizePositive(BigDecimal amount) {
        if (amount == null) throw new IllegalArgumentException("Amount is required");
        BigDecimal abs = amount.abs();
        if (abs.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Income amount must be greater than zero");
        }
        return abs;
    }
}
