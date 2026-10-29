package com.homestay.platform.web.dto;

import com.homestay.platform.model.Booking;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Form backing object for creating / editing a booking. */
public class BookingForm {

    @NotBlank(message = "Guest name is required")
    @Size(max = 100, message = "Guest name must be at most 100 characters")
    private String guestName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "\\d{10}", message = "Phone must be exactly 10 digits")
    private String guestPhone;

    @Email(message = "Enter a valid email address")
    @Size(max = 120)
    private String guestEmail;

    @NotNull(message = "Select a property")
    private Long propertyId;

    @NotNull(message = "Check-in date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkIn;

    @NotNull(message = "Check-out date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkOut;

    @Min(value = 1, message = "At least 1 guest")
    @Max(value = 20, message = "At most 20 guests")
    private int guests = 1;

    @Size(max = 500, message = "Notes must be at most 500 characters")
    private String notes;

    public static BookingForm from(Booking b) {
        BookingForm f = new BookingForm();
        f.guestName = b.getGuestName();
        f.guestPhone = b.getGuestPhone();
        f.guestEmail = b.getGuestEmail();
        f.propertyId = b.getProperty().getId();
        f.checkIn = b.getCheckIn();
        f.checkOut = b.getCheckOut();
        f.guests = b.getGuests();
        f.notes = b.getNotes();
        return f;
    }

    /** Cross-field rule: check-out must be after check-in. */
    public boolean hasValidDateRange() {
        return checkIn == null || checkOut == null || checkOut.isAfter(checkIn);
    }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
    public String getGuestPhone() { return guestPhone; }
    public void setGuestPhone(String guestPhone) { this.guestPhone = guestPhone; }
    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }
    public Long getPropertyId() { return propertyId; }
    public void setPropertyId(Long propertyId) { this.propertyId = propertyId; }
    public LocalDate getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDate checkIn) { this.checkIn = checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDate checkOut) { this.checkOut = checkOut; }
    public int getGuests() { return guests; }
    public void setGuests(int guests) { this.guests = guests; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
