package com.example.quizapp.web;

import com.example.quizapp.security.UserRegistrationDto;
import com.example.quizapp.security.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthWebController {

    private final UserService userService;

//    Show login page
    @GetMapping("/login")
    public String showLoginPage(Model model) {
        log.info("GET /login - Showing login page");
        model.addAttribute("title", "Login");
        return "auth/login";
    }

//    Show registration page
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        log.info("GET /register - Showing registration page");
        model.addAttribute("registrationDto", new UserRegistrationDto());
        model.addAttribute("title", "Register");
        return "auth/register";
    }

//    Process registration
    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("registrationDto") UserRegistrationDto registrationDto,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes,
                                      Model model) {
        log.info("POST /register - Processing registration for: {}", registrationDto.getUsername());

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors during registration: {}", bindingResult.getAllErrors());
            model.addAttribute("title", "Register");
            return "auth/register";
        }

        // Check if username already exists
        if (userService.usernameExists(registrationDto.getUsername())) {
            log.warn("Username already exists: {}", registrationDto.getUsername());
            bindingResult.rejectValue("username", "error.username", "Username is already taken");
            model.addAttribute("title", "Register");
            return "auth/register";
        }

        // Check if email already exists
        if (userService.emailExists(registrationDto.getEmail())) {
            log.warn("Email already exists: {}", registrationDto.getEmail());
            bindingResult.rejectValue("email", "error.email", "Email is already registered");
            model.addAttribute("title", "Register");
            return "auth/register";
        }

        try {
            userService.createUser(registrationDto);
            log.info("User registered successfully: {}", registrationDto.getUsername());
            redirectAttributes.addFlashAttribute("message", "Registration successful! Please login.");
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            log.error("Error during registration: {}", e.getMessage());
            model.addAttribute("error", "Registration failed. Please try again.");
            model.addAttribute("title", "Register");
            return "auth/register";
        }
    }
}