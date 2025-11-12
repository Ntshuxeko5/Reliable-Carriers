package com.reliablecarriers.Reliable.Carriers.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN') or hasRole('TRACKING_MANAGER')")
public class AdminController {

    @GetMapping("/packages")
    public String packagesPage() {
        return "admin/packages";
    }

    /**
     * Comprehensive Package Management Interface
     */
    @GetMapping("/package-management")
    public String packageManagement() {
        return "admin/package-management";
    }

    /**
     * System Settings Page
     */
    @GetMapping("/settings")
    public String settings() {
        return "admin/settings";
    }
}