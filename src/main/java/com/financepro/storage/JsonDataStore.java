package com.financepro.storage;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.financepro.entity.*;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The single source of truth for application data. Loads {@code project.json}
 * at startup, keeps everything in memory, and flushes to disk on every
 * mutation. Replaces what was previously a MySQL + JPA stack.
 *
 * Concurrency: all reads and writes go through a single {@link ReentrantLock}
 * — fine-grained for a personal finance app and simpler than RW-locks.
 */
@Component
@Slf4j
public class JsonDataStore {

    private final ObjectMapper mapper;
    private final Path filePath;
    private final boolean prettyPrint;

    private final ReentrantLock lock = new ReentrantLock();
    private RootDocument data = new RootDocument();

    public JsonDataStore(@Value("${app.data.file:project.json}") String fileLocation,
                         @Value("${app.data.pretty-print:true}") boolean prettyPrint) {
        this.filePath = Paths.get(fileLocation).toAbsolutePath();
        this.prettyPrint = prettyPrint;
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        if (prettyPrint) {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        }
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────

    @PostConstruct
    public void load() {
        lock.lock();
        try {
            if (Files.exists(filePath)) {
                try {
                    data = mapper.readValue(filePath.toFile(), RootDocument.class);
                    if (data == null) data = new RootDocument();
                    log.info("Loaded project data from {} (users={}, expenses={}, income={}, goals={})",
                            filePath, data.users.size(), data.expenses.size(),
                            data.incomes.size(), data.savingsGoals.size());
                } catch (IOException ex) {
                    log.error("Failed to read {} — starting with an empty store", filePath, ex);
                    data = new RootDocument();
                }
            } else {
                log.info("No existing project file at {} — a fresh store will be created on first write", filePath);
            }
        } finally {
            lock.unlock();
        }
    }

    /** Flushes the in-memory state to disk. Caller MUST already hold the lock. */
    private void persistLocked() {
        try {
            Path parent = filePath.getParent();
            if (parent != null) Files.createDirectories(parent);
            Path tmp = filePath.resolveSibling(filePath.getFileName().toString() + ".tmp");
            mapper.writeValue(tmp.toFile(), data);
            try {
                Files.move(tmp, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(tmp, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to persist project data to " + filePath, ex);
        }
    }

    // ── Public access helpers ────────────────────────────────────────────────

    /** Runs the supplied action under the store lock and persists after it returns. */
    public <T> T mutate(java.util.function.Function<RootDocument, T> action) {
        lock.lock();
        try {
            T result = action.apply(data);
            persistLocked();
            return result;
        } finally {
            lock.unlock();
        }
    }

    /** Read-only access; the returned copy is a defensive snapshot. */
    public <T> T read(java.util.function.Function<RootDocument, T> action) {
        lock.lock();
        try {
            return action.apply(data);
        } finally {
            lock.unlock();
        }
    }

    /** Next id for the given collection name (users / expenses / etc.). */
    public long nextId(String entity) {
        AtomicLong seq = data.sequences.computeIfAbsent(entity, k -> new AtomicLong(0));
        return seq.incrementAndGet();
    }

    public Path getFilePath() {
        return filePath;
    }

    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    // ── Root document ────────────────────────────────────────────────────────

    /**
     * Top-level shape of {@code project.json}. Lists are public so repository
     * classes can append / remove items directly while holding the store lock.
     */
    /**
     * Note: there is no {@code transactions} collection — the unified
     * transaction list is derived from {@code expenses} + {@code incomes}
     * inside {@link com.financepro.repository.TransactionRepository}.
     */
    @Data
    @NoArgsConstructor
    public static class RootDocument {
        private Map<String, AtomicLong> sequences = new HashMap<>();
        private List<User>         users        = new ArrayList<>();
        private List<Expense>      expenses     = new ArrayList<>();
        private List<Income>       incomes      = new ArrayList<>();
        private List<SavingsGoal>  savingsGoals = new ArrayList<>();
    }
}
