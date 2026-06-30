package com.javaproj.ToolMates.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageRouteController {

    @GetMapping("/")
    public String home() {
        return "forward:/dashboard.html";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "forward:/dashboard.html";
    }

    @GetMapping("/login")
    public String login() {
        return "forward:/login.html";
    }

    @GetMapping("/signup")
    public String signup() {
        return "forward:/signup.html";
    }

    @GetMapping("/rental-history")
    public String rentalHistory() {
        return "forward:/RentalHistory.html";
    }

    @GetMapping("/rental-details")
    public String rentalDetails() {
        return "forward:/RentalDetails.html";
    }
}
