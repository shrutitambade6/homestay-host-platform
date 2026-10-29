package com.homestay.platform.service;

import com.homestay.platform.model.Booking;
import com.homestay.platform.model.BookingStatus;
import com.homestay.platform.model.Property;
import com.homestay.platform.repository.BookingRepository;
import com.homestay.platform.repository.PropertyRepository;
import com.homestay.platform.web.dto.BookingForm;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class BookingService {

    private final BookingRepository bookings;
    private final PropertyRepository properties;

    public BookingService(BookingRepository bookings, PropertyRepository properties) {
        this.bookings = bookings;
        this.properties = properties;
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
}
