package com.reliablecarriers.Reliable.Carriers.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/tracking")
public class TrackingWebController {

    /**
     * Main tracking dashboard with map view
     */
    @GetMapping("/dashboard")
    public String trackingDashboard(Model model) {
        model.addAttribute("pageTitle", "Driver Tracking Dashboard");
        model.addAttribute("activePage", "tracking-dashboard");
        return "tracking/dashboard";
    }

    /**
     * Real-time map view
     */
    @GetMapping("/map")
    public String realTimeMap(Model model) {
        model.addAttribute("pageTitle", "Real-Time Driver Map");
        model.addAttribute("activePage", "tracking-map");
        return "tracking/map";
    }

    /**
     * Driver history view
     */
    @GetMapping("/driver-history")
    public String driverHistory(Model model) {
        model.addAttribute("pageTitle", "Driver Location History");
        model.addAttribute("activePage", "tracking-history");
        return "tracking/driver-history";
    }

    /**
     * Vehicle tracking view
     */
    @GetMapping("/vehicle-tracking")
    public String vehicleTracking(Model model) {
        model.addAttribute("pageTitle", "Vehicle Tracking");
        model.addAttribute("activePage", "tracking-vehicles");
        return "tracking/vehicle-tracking";
    }

    /**
     * Analytics and reports view
     */
    @GetMapping("/analytics")
    public String analytics(Model model) {
        model.addAttribute("pageTitle", "Tracking Analytics");
        model.addAttribute("activePage", "tracking-analytics");
        return "tracking/analytics";
    }

    /**
     * Settings and configuration view
     */
    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("pageTitle", "Tracking Settings");
        model.addAttribute("activePage", "tracking-settings");
        return "tracking/settings";
    }
}
