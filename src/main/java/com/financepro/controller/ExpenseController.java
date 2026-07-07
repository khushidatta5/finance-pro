package com.financepro.controller;

import com.financepro.dto.ExpenseDTO;
import com.financepro.entity.Expense;
import com.financepro.entity.User;
import com.financepro.service.ExpenseService;
import com.financepro.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * MVC controller for Expense pages — full CRUD via Thymeleaf views.
 */
@Controller
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final UserService userService;

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails principal, Model model) {
        User user = userService.getByUsername(principal.getUsername());
        List<Expense> expenses = expenseService.getAllByUser(user.getId());
        model.addAttribute("user", user);
        model.addAttribute("expenses", expenses);
        model.addAttribute("activePage", "expenses");
        model.addAttribute("expenseDTO", new ExpenseDTO());
        return "expenses";
    }

    @PostMapping("/add")
    public String add(@AuthenticationPrincipal UserDetails principal,
                      @Valid @ModelAttribute("expenseDTO") ExpenseDTO dto,
                      BindingResult result,
                      RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("error", "Please fix validation errors.");
            return "redirect:/expenses";
        }
        User user = userService.getByUsername(principal.getUsername());
        expenseService.create(dto, user.getId());
        ra.addFlashAttribute("success", "Expense added successfully!");
        return "redirect:/expenses";
    }

    @PostMapping("/edit/{id}")
    public String edit(@AuthenticationPrincipal UserDetails principal,
                       @PathVariable Long id,
                       @Valid @ModelAttribute("expenseDTO") ExpenseDTO dto,
                       BindingResult result,
                       RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("error", "Please fix validation errors.");
            return "redirect:/expenses";
        }
        User user = userService.getByUsername(principal.getUsername());
        expenseService.update(id, dto, user.getId());
        ra.addFlashAttribute("success", "Expense updated successfully!");
        return "redirect:/expenses";
    }

    @PostMapping("/delete/{id}")
    public String delete(@AuthenticationPrincipal UserDetails principal,
                         @PathVariable Long id,
                         RedirectAttributes ra) {
        User user = userService.getByUsername(principal.getUsername());
        expenseService.delete(id, user.getId());
        ra.addFlashAttribute("success", "Expense deleted.");
        return "redirect:/expenses";
    }
}
