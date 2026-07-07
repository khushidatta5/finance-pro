package com.financepro.repository;

import com.financepro.entity.User;
import com.financepro.storage.JsonDataStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * In-memory / JSON-backed replacement for the previous Spring-Data JPA
 * repository. Public surface is intentionally narrow — only the methods
 * actually called by services are exposed.
 */
@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final JsonDataStore store;

    public Optional<User> findByUsername(String username) {
        return store.read(d -> d.getUsers().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst());
    }

    public Optional<User> findByEmail(String email) {
        return store.read(d -> d.getUsers().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst());
    }

    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    public Optional<User> findById(Long id) {
        return store.read(d -> d.getUsers().stream()
                .filter(u -> id.equals(u.getId()))
                .findFirst());
    }

    public List<User> findAll() {
        return store.read(d -> new ArrayList<>(d.getUsers()));
    }

    public User save(User user) {
        return store.mutate(d -> {
            LocalDateTime now = LocalDateTime.now();
            if (user.getId() == null) {
                user.setId(store.nextId("users"));
                user.setCreatedAt(now);
                user.setUpdatedAt(now);
                d.getUsers().add(user);
            } else {
                user.setUpdatedAt(now);
                d.getUsers().removeIf(u -> u.getId().equals(user.getId()));
                d.getUsers().add(user);
            }
            return user;
        });
    }

    public void deleteById(Long id) {
        store.mutate(d -> {
            d.getUsers().removeIf(u -> id.equals(u.getId()));
            d.getExpenses().removeIf(e -> id.equals(e.getUserId()));
            d.getIncomes().removeIf(i -> id.equals(i.getUserId()));
            d.getSavingsGoals().removeIf(g -> id.equals(g.getUserId()));
            return null;
        });
    }
}
