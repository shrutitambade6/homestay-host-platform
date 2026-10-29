package com.homestay.platform.web;

import com.homestay.platform.model.BookingStatus;
import com.homestay.platform.service.BookingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final BookingService bookingService;

    public HomeController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("summary", bookingService.dashboard());
        model.addAttribute("statuses", BookingStatus.values());
        return "dashboard";
    }
}
