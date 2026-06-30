package com.javaproj.ToolMates.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageRouteController {

    @GetMapping("/rental-history")
    public String rentalHistory() {
        return "forward:/RentalHistory.html";
    }

    @GetMapping("/rental-details")
    public String rentalDetails() {
        return "forward:/RentalDetails.html";
    }
}
