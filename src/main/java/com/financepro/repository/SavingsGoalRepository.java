package com.financepro.repository;

import com.financepro.entity.SavingsGoal;
import com.financepro.storage.JsonDataStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JSON-backed repository for {@link SavingsGoal}.
 */
@Repository
@RequiredArgsConstructor
public class SavingsGoalRepository {

    private final JsonDataStore store;

    public List<SavingsGoal> findByUserIdOrderByCreatedAtDesc(Long userId) {
        return store.read(d -> d.getSavingsGoals().stream()
                .filter(g -> userId.equals(g.getUserId()))
                .sorted(Comparator.comparing(SavingsGoal::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList()));
    }

    public Optional<SavingsGoal> findById(Long id) {
        return store.read(d -> d.getSavingsGoals().stream()
                .filter(g -> id.equals(g.getId()))
                .findFirst());
    }

    public SavingsGoal save(SavingsGoal goal) {
        return store.mutate(d -> {
            if (goal.getId() == null) {
                goal.setId(store.nextId("savings_goals"));
                goal.setCreatedAt(LocalDateTime.now());
                d.getSavingsGoals().add(goal);
            } else {
                d.getSavingsGoals().removeIf(g -> g.getId().equals(goal.getId()));
                d.getSavingsGoals().add(goal);
            }
            return goal;
        });
    }

    public void delete(SavingsGoal goal) {
        store.mutate(d -> {
            d.getSavingsGoals().removeIf(g -> g.getId().equals(goal.getId()));
            return null;
        });
    }
}
