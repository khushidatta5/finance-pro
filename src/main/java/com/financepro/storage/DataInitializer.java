package com.financepro.storage;

import com.financepro.entity.*;
import com.financepro.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Seeds {@code project.json} with two demo users (admin / demo, both with
 * password {@code admin123}) plus a handful of sample expense, income, and
 * savings-goal records the very first time the app starts. After that the
 * file already exists so this initializer is a no-op.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.findAll().isEmpty()) {
            log.info("Existing data found — skipping seed");
            return;
        }
        log.info("Empty project.json detected — seeding demo users and sample data");

        User admin = userRepository.save(User.builder()
                .username("admin")
                .email("admin@financepro.com")
                .fullName("Alex Johnson")
                .password(passwordEncoder.encode("admin123"))
                .currency("INR")
                .darkMode(false)
                .role("ADMIN")
                .build());

        userRepository.save(User.builder()
                .username("demo")
                .email("demo@financepro.com")
                .fullName("Sam Wilson")
                .password(passwordEncoder.encode("admin123"))
                .currency("INR")
                .darkMode(false)
                .role("USER")
                .build());

        Long uid = admin.getId();
        LocalDate today = LocalDate.now();

        seedExpense(uid, "Grocery Shopping",  "1200.50", "FOOD",     today.minusDays(2),  "Weekly groceries");
        seedExpense(uid, "Netflix",           "199.00",  "BILLS",    today.minusDays(3),  "Monthly streaming");
        seedExpense(uid, "Petrol",            "1500.00", "TRAVEL",   today.minusDays(4),  "Fuel refill");
        seedExpense(uid, "Amazon Order",      "899.99",  "SHOPPING", today.minusDays(5),  "Electronics accessories");
        seedExpense(uid, "Restaurant",        "850.00",  "FOOD",     today.minusDays(7),  "Dinner with friends");
        seedExpense(uid, "Electricity Bill",  "2450.00", "BILLS",    today.minusDays(8),  "Monthly electricity");
        seedExpense(uid, "Uber Ride",         "250.00",  "TRAVEL",   today.minusDays(10), "Airport trip");
        seedExpense(uid, "Clothing Store",    "2100.00", "SHOPPING", today.minusDays(12), "New shirts");

        seedIncome(uid, "Monthly Salary",    "75000.00", "SALARY",      today.withDayOfMonth(1), "Salary credited");
        seedIncome(uid, "Freelance Project", "12500.00", "FREELANCING", today.minusDays(6),       "Logo design gig");
        seedIncome(uid, "Stock Dividends",   "2400.00",  "INVESTMENTS", today.minusDays(15),      "Q4 dividends");

        savingsGoalRepository.save(SavingsGoal.builder()
                .title("Emergency Fund")
                .targetAmount(new BigDecimal("100000.00"))
                .currentAmount(new BigDecimal("35000.00"))
                .deadline(today.plusMonths(6))
                .userId(uid)
                .build());
        savingsGoalRepository.save(SavingsGoal.builder()
                .title("Trip to Goa")
                .targetAmount(new BigDecimal("40000.00"))
                .currentAmount(new BigDecimal("12000.00"))
                .deadline(today.plusMonths(4))
                .userId(uid)
                .build());

        log.info("Seed complete — login as admin/admin123 or demo/admin123");
    }

    private void seedExpense(Long uid, String title, String amount, String category,
                             LocalDate date, String notes) {
        expenseRepository.save(Expense.builder()
                .title(title)
                .amount(new BigDecimal(amount))
                .category(category)
                .expenseDate(date)
                .notes(notes)
                .userId(uid)
                .build());
    }

    private void seedIncome(Long uid, String title, String amount, String source,
                            LocalDate date, String notes) {
        incomeRepository.save(Income.builder()
                .title(title)
                .amount(new BigDecimal(amount))
                .source(source)
                .incomeDate(date)
                .notes(notes)
                .userId(uid)
                .build());
    }
}
