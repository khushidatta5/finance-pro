package com.financepro.controller;

import com.financepro.dto.IncomeDTO;
import com.financepro.entity.Income;
import com.financepro.entity.User;
import com.financepro.service.IncomeService;
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
 * MVC controller for Income pages — full CRUD via Thymeleaf views.
 */
@Controller
@RequestMapping("/income")
@RequiredArgsConstructor
public class IncomeController {

    private final IncomeService incomeService;
    private final UserService userService;

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails principal, Model model) {
        User user = userService.getByUsername(principal.getUsername());
        List<Income> incomes = incomeService.getAllByUser(user.getId());
        model.addAttribute("user", user);
        model.addAttribute("incomes", incomes);
        model.addAttribute("activePage", "income");
        model.addAttribute("incomeDTO", new IncomeDTO());
        return "income";
    }

    @PostMapping("/add")
    public String add(@AuthenticationPrincipal UserDetails principal,
                      @Valid @ModelAttribute("incomeDTO") IncomeDTO dto,
                      BindingResult result,
                      RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("error", "Please fix validation errors.");
            return "redirect:/income";
        }
        User user = userService.getByUsername(principal.getUsername());
        incomeService.create(dto, user.getId());
        ra.addFlashAttribute("success", "Income record added successfully!");
        return "redirect:/income";
    }

    @PostMapping("/edit/{id}")
    public String edit(@AuthenticationPrincipal UserDetails principal,
                       @PathVariable Long id,
                       @Valid @ModelAttribute("incomeDTO") IncomeDTO dto,
                       BindingResult result,
                       RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("error", "Please fix validation errors.");
            return "redirect:/income";
        }
        User user = userService.getByUsername(principal.getUsername());
        incomeService.update(id, dto, user.getId());
        ra.addFlashAttribute("success", "Income record updated!");
        return "redirect:/income";
    }

    @PostMapping("/delete/{id}")
    public String delete(@AuthenticationPrincipal UserDetails principal,
                         @PathVariable Long id,
                         RedirectAttributes ra) {
        User user = userService.getByUsername(principal.getUsername());
        incomeService.delete(id, user.getId());
        ra.addFlashAttribute("success", "Income record deleted.");
        return "redirect:/income";
    }
}
