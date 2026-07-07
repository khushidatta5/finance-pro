package com.financepro.controller;

import com.financepro.dto.PagedResult;
import com.financepro.entity.Transaction;
import com.financepro.entity.User;
import com.financepro.service.TransactionService;
import com.financepro.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * MVC controller for the Transactions page — paged, filtered listing + CSV export.
 */
@Controller
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService;

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails principal,
                       @RequestParam(defaultValue = "") String search,
                       @RequestParam(defaultValue = "") String type,
                       @RequestParam(defaultValue = "") String category,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        User user = userService.getByUsername(principal.getUsername());
        PagedResult<Transaction> txPage = transactionService.getFiltered(
                user.getId(), type, category, search, page, 15);
        model.addAttribute("user", user);
        model.addAttribute("transactions", txPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", txPage.getTotalPages());
        model.addAttribute("totalElements", txPage.getTotalElements());
        model.addAttribute("search", search);
        model.addAttribute("type", type);
        model.addAttribute("category", category);
        model.addAttribute("activePage", "transactions");
        return "transactions";
    }

    /** Exports all user transactions as a downloadable CSV file. */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv(@AuthenticationPrincipal UserDetails principal) {
        User user = userService.getByUsername(principal.getUsername());
        List<Transaction> all = transactionService.getAllByUser(user.getId());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter pw = new PrintWriter(baos, true, StandardCharsets.UTF_8)) {
            pw.println("ID,Title,Amount (INR),Type,Category,Date,Notes");
            for (Transaction t : all) {
                pw.printf("%d,\"%s\",%.2f,%s,%s,%s,\"%s\"%n",
                        t.getId(),
                        safe(t.getTitle()),
                        t.getAmount(),
                        nullSafe(t.getType()),
                        nullSafe(t.getCategory()),
                        t.getTransactionDate(),
                        safe(t.getNotes()));
            }
        }

        byte[] csv = baos.toByteArray();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(csv.length)
                .body(csv);
    }

    private static String safe(String s) {
        return s == null ? "" : s.replace("\"", "\"\"");
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
