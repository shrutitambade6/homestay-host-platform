package com.homestay.platform;

import com.homestay.platform.model.Booking;
import com.homestay.platform.model.BookingStatus;
import com.homestay.platform.model.Property;
import com.homestay.platform.repository.PropertyRepository;
import com.homestay.platform.service.BookingService;
import com.homestay.platform.web.dto.BookingForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceTest {

    @Autowired
    BookingService service;

    @Autowired
    PropertyRepository properties;

    private BookingForm form(String guest, Property p, int nights) {
        BookingForm f = new BookingForm();
        f.setGuestName(guest);
        f.setGuestPhone("9876543210");
        f.setPropertyId(p.getId());
        f.setCheckIn(LocalDate.now().plusDays(10));
        f.setCheckOut(LocalDate.now().plusDays(10 + nights));
        f.setGuests(2);
        return f;
    }

    private Property property() {
        return properties.save(new Property("Test Villa", "Pune", 2, new BigDecimal("1000")));
    }

    @Test
    void createCalculatesAmountAndStartsPending() {
        Booking b = service.create(form("Unit Test Guest", property(), 3), "host");

        assertThat(b.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(b.getTotalAmount()).isEqualByComparingTo("3000");
        assertThat(b.getBookingRef()).startsWith("HS-");
        assertThat(b.getCreatedBy()).isEqualTo("host");
    }

    @Test
    void updateRecalculatesAmount() {
        Booking b = service.create(form("Update Guest", property(), 1), "host");
        BookingForm f = BookingForm.from(b);
        f.setCheckOut(f.getCheckIn().plusDays(4));

        Booking updated = service.update(b.getId(), f, "staff");

        assertThat(updated.getTotalAmount()).isEqualByComparingTo("4000");
        assertThat(updated.getLastUpdatedBy()).isEqualTo("staff");
    }

    @Test
    void searchFindsByNamePhoneAndStatus() {
        Booking b = service.create(form("Zara Searchable", property(), 1), "host");

        assertThat(service.search("zara search", null, null)).extracting(Booking::getId).contains(b.getId());
        assertThat(service.search(b.getBookingRef(), null, null)).hasSize(1);
        assertThat(service.search("Zara", BookingStatus.CANCELLED, null)).isEmpty();
        assertThat(service.search("", BookingStatus.PENDING, b.getProperty().getId())).hasSize(1);
    }

}
