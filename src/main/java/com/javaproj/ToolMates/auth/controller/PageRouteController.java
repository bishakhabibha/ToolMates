package com.javaproj.ToolMates.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PageRouteController {

    @GetMapping("/")
    public String home() {
        return "forward:/dashboard.html";
    }

    @GetMapping("/favicon.ico")
    @ResponseBody
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.noContent().build();
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
