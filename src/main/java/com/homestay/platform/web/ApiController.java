package com.homestay.platform.web;

import com.homestay.platform.model.Booking;
import com.homestay.platform.model.BookingStatus;
import com.homestay.platform.service.BookingService;
import com.homestay.platform.service.DashboardSummary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Small read-only JSON API. /api/version is public (used by health checks and
 * rollback verification); the rest needs HTTP Basic or a logged-in session.
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final BookingService bookingService;
    private final String version;
    private final String environment;

    public ApiController(BookingService bookingService,
                         @Value("${app.version:dev}") String version,
                         @Value("${app.environment:dev}") String environment) {
        this.bookingService = bookingService;
        this.version = version;
        this.environment = environment;
    }

    @GetMapping("/version")
    public Map<String, String> version() {
        return Map.of("application", "homestay-host-platform", "version", version, "environment", environment);
    }

    @GetMapping("/bookings")
    public List<BookingDto> bookings(@RequestParam(required = false) String q,
                                     @RequestParam(required = false) BookingStatus status,
                                     @RequestParam(required = false) Long propertyId) {
        return bookingService.search(q, status, propertyId).stream().map(BookingDto::of).toList();
    }

    @GetMapping("/bookings/{id}")
    public BookingDto booking(@PathVariable Long id) {
        return BookingDto.of(bookingService.get(id));
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        DashboardSummary s = bookingService.dashboard();
        return Map.of(
                "totalBookings", s.totalBookings(),
                "countsByStatus", s.countsByStatus(),
                "todaysCheckIns", s.todaysCheckIns(),
                "guestsInHouse", s.guestsInHouse(),
                "revenue", s.revenue(),
                "occupancyPercent", s.occupancyPercent());
    }

    public record BookingDto(Long id, String bookingRef, String guestName, String guestPhone,
                             String property, LocalDate checkIn, LocalDate checkOut, int guests,
                             BigDecimal totalAmount, BookingStatus status) {
        static BookingDto of(Booking b) {
            return new BookingDto(b.getId(), b.getBookingRef(), b.getGuestName(), b.getGuestPhone(),
                    b.getProperty().getName(), b.getCheckIn(), b.getCheckOut(), b.getGuests(),
                    b.getTotalAmount(), b.getStatus());
        }
    }
}
