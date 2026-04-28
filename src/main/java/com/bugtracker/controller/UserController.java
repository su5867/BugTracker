package com.bugtracker.controller;

import com.bugtracker.dto.UserDto;
import com.bugtracker.entity.User;
import com.bugtracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/users")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        return "user/list";
    }

    @GetMapping("/new")
    public String newUserForm(Model model) {
        model.addAttribute("userDto", new UserDto());
        return "user/form";
    }

    @PostMapping
    public String createUser(@Valid @ModelAttribute("userDto") UserDto dto,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (result.hasErrors()) return "user/form";
        try {
            userService.registerUser(dto);
            redirectAttributes.addFlashAttribute("success", "User created successfully.");
            return "redirect:/users";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "user/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setEnabled(user.isEnabled());
        dto.setRole(user.getPrimaryRoleName());
        model.addAttribute("userDto", dto);
        model.addAttribute("user",    user);
        return "user/form";
    }

    @PostMapping("/{id}/update")
    public String updateUser(@PathVariable Long id,
                              @Valid @ModelAttribute("userDto") UserDto dto,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("user", userService.findById(id));
            return "user/form";
        }
        userService.updateUser(id, dto);
        redirectAttributes.addFlashAttribute("success", "User updated successfully.");
        return "redirect:/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("success", "User deleted successfully.");
        return "redirect:/users";
    }
}
