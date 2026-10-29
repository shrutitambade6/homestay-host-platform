package com.homestay.platform.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /** The dashboard arrives in feature/workflow-dashboard; until then start on the bookings list. */
    @GetMapping("/")
    public String home() {
        return "redirect:/bookings";
    }
}
