package com.financepro.controller;

import com.financepro.entity.User;
import com.financepro.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles the Settings page — profile update, password change, preferences.
 */
@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final UserService userService;

    @GetMapping
    public String page(@AuthenticationPrincipal UserDetails principal, Model model) {
        User user = userService.getByUsername(principal.getUsername());
        model.addAttribute("user", user);
        model.addAttribute("activePage", "settings");
        return "settings";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal UserDetails principal,
                                @RequestParam String fullName,
                                @RequestParam String currency,
                                @RequestParam(defaultValue = "false") boolean darkMode,
                                RedirectAttributes ra) {
        User user = userService.getByUsername(principal.getUsername());
        userService.updateSettings(user.getId(), currency, darkMode, fullName);
        ra.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/settings";
    }

    @PostMapping("/password")
    public String changePassword(@AuthenticationPrincipal UserDetails principal,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes ra) {
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "New passwords do not match.");
            return "redirect:/settings";
        }
        try {
            User user = userService.getByUsername(principal.getUsername());
            userService.changePassword(user.getId(), currentPassword, newPassword);
            ra.addFlashAttribute("success", "Password changed successfully!");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/settings";
    }
}
