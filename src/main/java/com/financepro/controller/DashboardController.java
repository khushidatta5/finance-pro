package com.financepro.controller;

import com.financepro.dto.DashboardSummaryDTO;
import com.financepro.entity.User;
import com.financepro.service.DashboardService;
import com.financepro.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the main dashboard page with aggregated financial summary.
 */
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails principal, Model model) {
        User user = userService.getByUsername(principal.getUsername());
        DashboardSummaryDTO summary = dashboardService.buildSummary(user.getId());
        model.addAttribute("user", user);
        model.addAttribute("summary", summary);
        model.addAttribute("activePage", "dashboard");
        return "dashboard";
    }

    @GetMapping("/analytics")
    public String analytics(@AuthenticationPrincipal UserDetails principal, Model model) {
        User user = userService.getByUsername(principal.getUsername());
        DashboardSummaryDTO summary = dashboardService.buildSummary(user.getId());
        model.addAttribute("user", user);
        model.addAttribute("summary", summary);
        model.addAttribute("activePage", "analytics");
        return "analytics";
    }
}
