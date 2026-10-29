package com.homestay.platform.web;

import com.homestay.platform.model.Booking;
import com.homestay.platform.model.BookingStatus;
import com.homestay.platform.service.BookingService;
import com.homestay.platform.service.PropertyService;
import com.homestay.platform.service.WorkflowException;
import com.homestay.platform.web.dto.BookingForm;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final PropertyService propertyService;

    public BookingController(BookingService bookingService, PropertyService propertyService) {
        this.bookingService = bookingService;
        this.propertyService = propertyService;
    }

    /** List + search (free text, status, property). */
    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) BookingStatus status,
                       @RequestParam(required = false) Long propertyId,
                       Model model) {
        model.addAttribute("bookings", bookingService.search(q, status, propertyId));
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("status", status);
        model.addAttribute("propertyId", propertyId);
        model.addAttribute("statuses", BookingStatus.values());
        model.addAttribute("properties", propertyService.findAll());
        return "bookings/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new BookingForm());
        return formView(model, null);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") BookingForm form, BindingResult result,
                         Authentication auth, Model model, RedirectAttributes flash) {
        checkDates(form, result);
        if (result.hasErrors()) {
            return formView(model, null);
        }
        Booking saved = bookingService.create(form, auth.getName());
        flash.addFlashAttribute("success", "Booking " + saved.getBookingRef() + " created");
        return "redirect:/bookings/" + saved.getId();
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        Booking b = bookingService.get(id);
        model.addAttribute("booking", b);
        return "bookings/view";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes flash) {
        Booking b = bookingService.get(id);
        if (!b.getStatus().isEditable()) {
            flash.addFlashAttribute("error", "A " + b.getStatus().getLabel().toLowerCase()
                    + " booking can no longer be edited");
            return "redirect:/bookings/" + id;
        }
        model.addAttribute("form", BookingForm.from(b));
        return formView(model, b);
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") BookingForm form,
                         BindingResult result, Authentication auth, Model model, RedirectAttributes flash) {
        checkDates(form, result);
        if (result.hasErrors()) {
            return formView(model, bookingService.get(id));
        }
        try {
            Booking saved = bookingService.update(id, form, auth.getName());
            flash.addFlashAttribute("success", "Booking " + saved.getBookingRef() + " updated");
        } catch (WorkflowException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/bookings/" + id;
    }

    private void checkDates(BookingForm form, BindingResult result) {
        if (!form.hasValidDateRange()) {
            result.rejectValue("checkOut", "dateRange", "Check-out must be after check-in");
        }
    }

    private String formView(Model model, Booking existing) {
        model.addAttribute("booking", existing);
        model.addAttribute("properties", propertyService.findActive());
        return "bookings/form";
    }
}
