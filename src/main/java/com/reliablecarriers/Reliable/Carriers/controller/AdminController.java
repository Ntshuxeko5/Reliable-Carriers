package com.reliablecarriers.Reliable.Carriers.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/packages")
    public String packagesPage() {
        return "admin/packages";
    }
}