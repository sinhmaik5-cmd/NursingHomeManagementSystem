package com.group6.nhms.controller.web;

import com.group6.nhms.dto.request.RelativeRegisterRequest;
import com.group6.nhms.service.RelativeService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebAuthController {

    private final RelativeService relativeService;

    public WebAuthController(RelativeService relativeService) {
        this.relativeService = relativeService;
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            return "redirect:/";
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            return "redirect:/";
        }
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RelativeRegisterRequest());
        }
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(
            @Valid @ModelAttribute("registerRequest") RelativeRegisterRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            relativeService.registerRelative(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đăng ký tài khoản Người thân thành công! Bạn có thể đăng nhập ngay bây giờ.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/403")
    public String accessDenied() {
        return "error/403";
    }
}
