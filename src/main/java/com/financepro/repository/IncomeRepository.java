package com.financepro.repository;

import com.financepro.entity.Income;
import com.financepro.storage.JsonDataStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JSON-backed repository for {@link Income}.
 */
@Repository
@RequiredArgsConstructor
public class IncomeRepository {

    private final JsonDataStore store;

    public Optional<Income> findById(Long id) {
        return store.read(d -> d.getIncomes().stream()
                .filter(i -> id.equals(i.getId()))
                .findFirst());
    }

    public Income save(Income income) {
        return store.mutate(d -> {
            LocalDateTime now = LocalDateTime.now();
            if (income.getId() == null) {
                income.setId(store.nextId("incomes"));
                income.setCreatedAt(now);
                income.setUpdatedAt(now);
                d.getIncomes().add(income);
            } else {
                income.setUpdatedAt(now);
                d.getIncomes().removeIf(i -> i.getId().equals(income.getId()));
                d.getIncomes().add(income);
            }
            return income;
        });
    }

    public void delete(Income income) {
        store.mutate(d -> {
            d.getIncomes().removeIf(i -> i.getId().equals(income.getId()));
            return null;
        });
    }

    public List<Income> findByUserIdOrderByIncomeDateDesc(Long userId) {
        return store.read(d -> d.getIncomes().stream()
                .filter(i -> userId.equals(i.getUserId()))
                .sorted(Comparator.comparing(Income::getIncomeDate).reversed())
                .collect(Collectors.toList()));
    }

    public BigDecimal sumTotalByUserId(Long userId) {
        return store.read(d -> d.getIncomes().stream()
                .filter(i -> userId.equals(i.getUserId()))
                .map(Income::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
