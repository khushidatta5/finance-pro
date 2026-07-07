package com.financepro.controller;

import com.financepro.dto.*;
import com.financepro.entity.*;
import com.financepro.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API controller — JSON endpoints consumed by the frontend JavaScript.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final DashboardService dashboardService;
    private final ExpenseService expenseService;
    private final IncomeService incomeService;
    private final TransactionService transactionService;
    private final UserService userService;

    // ── Dashboard ────────────────────────────────────────────────────────────

    @GetMapping("/dashboard/summary")
    public ResponseEntity<DashboardSummaryDTO> summary(@AuthenticationPrincipal UserDetails p) {
        User user = userService.getByUsername(p.getUsername());
        return ResponseEntity.ok(dashboardService.buildSummary(user.getId()));
    }

    // ── Expenses ─────────────────────────────────────────────────────────────

    @GetMapping("/expenses")
    public ResponseEntity<List<ExpenseDTO>> listExpenses(@AuthenticationPrincipal UserDetails p) {
        User user = userService.getByUsername(p.getUsername());
        List<ExpenseDTO> dtos = expenseService.getAllByUser(user.getId()).stream()
                .map(e -> ExpenseDTO.builder()
                        .id(e.getId()).title(e.getTitle()).amount(e.getAmount())
                        .category(e.getCategory()).expenseDate(e.getExpenseDate())
                        .notes(e.getNotes()).build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/expenses")
    public ResponseEntity<ExpenseDTO> createExpense(@AuthenticationPrincipal UserDetails p,
                                                    @Valid @RequestBody ExpenseDTO dto) {
        User user = userService.getByUsername(p.getUsername());
        Expense saved = expenseService.create(dto, user.getId());
        dto.setId(saved.getId());
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/expenses/{id}")
    public ResponseEntity<ExpenseDTO> updateExpense(@AuthenticationPrincipal UserDetails p,
                                                    @PathVariable Long id,
                                                    @Valid @RequestBody ExpenseDTO dto) {
        User user = userService.getByUsername(p.getUsername());
        Expense saved = expenseService.update(id, dto, user.getId());
        dto.setId(saved.getId());
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<Void> deleteExpense(@AuthenticationPrincipal UserDetails p,
                                              @PathVariable Long id) {
        User user = userService.getByUsername(p.getUsername());
        expenseService.delete(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    // ── Income ───────────────────────────────────────────────────────────────

    @GetMapping("/income")
    public ResponseEntity<List<IncomeDTO>> listIncome(@AuthenticationPrincipal UserDetails p) {
        User user = userService.getByUsername(p.getUsername());
        List<IncomeDTO> dtos = incomeService.getAllByUser(user.getId()).stream()
                .map(i -> IncomeDTO.builder()
                        .id(i.getId()).title(i.getTitle()).amount(i.getAmount())
                        .source(i.getSource()).incomeDate(i.getIncomeDate())
                        .notes(i.getNotes()).build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/income")
    public ResponseEntity<IncomeDTO> createIncome(@AuthenticationPrincipal UserDetails p,
                                                  @Valid @RequestBody IncomeDTO dto) {
        User user = userService.getByUsername(p.getUsername());
        Income saved = incomeService.create(dto, user.getId());
        dto.setId(saved.getId());
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/income/{id}")
    public ResponseEntity<IncomeDTO> updateIncome(@AuthenticationPrincipal UserDetails p,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody IncomeDTO dto) {
        User user = userService.getByUsername(p.getUsername());
        Income saved = incomeService.update(id, dto, user.getId());
        dto.setId(saved.getId());
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/income/{id}")
    public ResponseEntity<Void> deleteIncome(@AuthenticationPrincipal UserDetails p,
                                             @PathVariable Long id) {
        User user = userService.getByUsername(p.getUsername());
        incomeService.delete(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    // ── Transactions ─────────────────────────────────────────────────────────

    @GetMapping("/transactions/recent")
    public ResponseEntity<List<TransactionDTO>> recentTransactions(@AuthenticationPrincipal UserDetails p) {
        User user = userService.getByUsername(p.getUsername());
        return ResponseEntity.ok(transactionService.toDTOList(transactionService.getRecentByUser(user.getId())));
    }
}
