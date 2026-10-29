package com.homestay.platform.service;

import com.homestay.platform.model.Booking;
import com.homestay.platform.model.BookingAction;
import com.homestay.platform.model.BookingStatus;
import com.homestay.platform.model.Property;
import com.homestay.platform.model.Role;
import com.homestay.platform.repository.BookingRepository;
import com.homestay.platform.repository.PropertyRepository;
import com.homestay.platform.web.dto.BookingForm;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class BookingService {

    /** Statuses that count as revenue and occupy a room. */
    static final Set<BookingStatus> ACTIVE_STATUSES =
            EnumSet.of(BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN, BookingStatus.CHECKED_OUT);
    static final Set<BookingStatus> OCCUPYING_STATUSES =
            EnumSet.of(BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN);
    static final Set<BookingStatus> UPCOMING_STATUSES =
            EnumSet.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final BookingRepository bookings;
    private final PropertyRepository properties;
    private final Clock clock;

    public BookingService(BookingRepository bookings, PropertyRepository properties, Clock clock) {
        this.bookings = bookings;
        this.properties = properties;
        this.clock = clock;
    }

    // ---------------------------------------------------------------- create / update / read

    public Booking create(BookingForm form, String username) {
        Booking b = new Booking();
        apply(form, b);
        b.setStatus(BookingStatus.PENDING);
        b.setCreatedBy(username);
        b.setLastUpdatedBy(username);
        Booking saved = bookings.save(b);
        saved.setBookingRef(String.format("HS-%05d", saved.getId()));
        return bookings.save(saved);
    }

    public Booking update(Long id, BookingForm form, String username) {
        Booking b = get(id);
        if (!b.getStatus().isEditable()) {
            throw new WorkflowException("Booking " + b.getBookingRef() + " is "
                    + b.getStatus().getLabel() + " and can no longer be edited");
        }
        apply(form, b);
        b.setLastUpdatedBy(username);
        return bookings.save(b);
    }

    @Transactional(readOnly = true)
    public Booking get(Long id) {
        return bookings.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public List<Booking> search(String q, BookingStatus status, Long propertyId) {
        String text = q == null ? "" : q.trim();
        return bookings.search(text, status, propertyId);
    }

    private void apply(BookingForm form, Booking b) {
        if (!form.hasValidDateRange()) {
            throw new IllegalArgumentException("Check-out must be after check-in");
        }
        Property property = properties.findById(form.getPropertyId())
                .orElseThrow(() -> new NotFoundException("Property " + form.getPropertyId() + " not found"));
        b.setGuestName(form.getGuestName().trim());
        b.setGuestPhone(form.getGuestPhone().trim());
        b.setGuestEmail(blankToNull(form.getGuestEmail()));
        b.setProperty(property);
        b.setCheckIn(form.getCheckIn());
        b.setCheckOut(form.getCheckOut());
        b.setGuests(form.getGuests());
        b.setNotes(blankToNull(form.getNotes()));
        b.setTotalAmount(property.getPricePerNight().multiply(BigDecimal.valueOf(b.getNights())));
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    // ---------------------------------------------------------------- role-based workflow

    public Booking applyAction(Long id, BookingAction action, Collection<Role> roles, String username) {
        Booking b = get(id);
        if (!action.isAllowedFor(roles)) {
            throw new WorkflowException("Your role is not allowed to " + action.getLabel().toLowerCase());
        }
        if (!action.canApplyTo(b.getStatus())) {
            throw new WorkflowException("Cannot " + action.getLabel().toLowerCase() + " a booking that is "
                    + b.getStatus().getLabel().toLowerCase());
        }
        b.setStatus(action.getTo());
        b.setLastUpdatedBy(username);
        return bookings.save(b);
    }

    public List<BookingAction> availableActions(Booking b, Collection<Role> roles) {
        return Arrays.stream(BookingAction.values())
                .filter(a -> a.isAvailable(b.getStatus(), roles))
                .toList();
    }

    // ---------------------------------------------------------------- dashboard

    @Transactional(readOnly = true)
    public DashboardSummary dashboard() {
        LocalDate today = LocalDate.now(clock);
        Map<BookingStatus, Long> counts = new EnumMap<>(BookingStatus.class);
        for (BookingStatus s : BookingStatus.values()) {
            counts.put(s, bookings.countByStatus(s));
        }
        long totalRooms = properties.totalActiveRooms();
        long occupied = bookings.countOccupying(today, OCCUPYING_STATUSES);
        int occupancy = totalRooms == 0 ? 0 : (int) Math.min(100, Math.round(occupied * 100.0 / totalRooms));

        return new DashboardSummary(
                bookings.count(),
                counts,
                bookings.countArrivals(today, UPCOMING_STATUSES),
                counts.get(BookingStatus.CHECKED_IN),
                bookings.sumAmountByStatusIn(ACTIVE_STATUSES),
                properties.countByActiveTrue(),
                totalRooms,
                occupied,
                occupancy,
                bookings.findUpcoming(today, UPCOMING_STATUSES, PageRequest.of(0, 5)));
    }
}
