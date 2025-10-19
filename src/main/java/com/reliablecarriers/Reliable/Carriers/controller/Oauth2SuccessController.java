package com.reliablecarriers.Reliable.Carriers.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

@Controller
public class Oauth2SuccessController {

    @GetMapping("/oauth2/success")
    public Object oauth2SuccessPage(@RequestParam(required = false) String token, @RequestParam(required = false) String role, HttpServletRequest request, Model model) {
        if (token != null) {
            model.addAttribute("token", token);
            model.addAttribute("role", role);
            return "oauth2-success";
        }
        // If no token provided, return a JSON fallback
        return ResponseEntity.ok().body(java.util.Map.of("message", "OAuth2 login successful"));
    }
}
