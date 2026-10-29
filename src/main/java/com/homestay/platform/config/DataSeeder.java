package com.homestay.platform.config;

import com.homestay.platform.model.Property;
import com.homestay.platform.repository.PropertyRepository;
import com.homestay.platform.service.BookingService;
import com.homestay.platform.web.dto.BookingForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

/** Loads demo properties and bookings on first start (disable with app.seed-demo-data=false). */
@Component
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final PropertyRepository properties;
    private final BookingService bookingService;
    private final Clock clock;

    public DataSeeder(PropertyRepository properties, BookingService bookingService, Clock clock) {
        this.properties = properties;
        this.bookingService = bookingService;
        this.clock = clock;
    }

    @Override
    public void run(String... args) {
        if (properties.count() > 0) {
            return;
        }
        log.info("Seeding demo data");
        Property hill = properties.save(new Property("Hilltop Cottage", "Mahabaleshwar", 4, new BigDecimal("3500")));
        Property sea = properties.save(new Property("Seaside Nest", "Alibaug", 3, new BigDecimal("4200")));
        Property farm = properties.save(new Property("Green Farm Stay", "Lonavala", 5, new BigDecimal("2800")));

        LocalDate today = LocalDate.now(clock);
        booking("Aarav Sharma", "9820011111", hill, today, today.plusDays(2), 2);
        booking("Priya Desai", "9820022222", sea, today.plusDays(3), today.plusDays(5), 3);
        booking("Rohan Patil", "9820033333", farm, today.minusDays(1), today.plusDays(1), 4);
        booking("Sneha Kulkarni", "9820044444", hill, today.minusDays(6), today.minusDays(3), 2);
        booking("Vikram Joshi", "9820055555", sea, today.plusDays(7), today.plusDays(9), 2);
        booking("Ananya Iyer", "9820066666", farm, today.plusDays(1), today.plusDays(4), 5);
    }

    private void booking(String guest, String phone, Property p, LocalDate in, LocalDate out, int guests) {
        BookingForm f = new BookingForm();
        f.setGuestName(guest);
        f.setGuestPhone(phone);
        f.setPropertyId(p.getId());
        f.setCheckIn(in);
        f.setCheckOut(out);
        f.setGuests(guests);
        bookingService.create(f, "seed");
    }
}
