package com.homestay.platform.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/** Journeys 2 and 3: create, update, validate and search bookings. */
class BookingManagementIT extends BaseSeleniumIT {

    private String createBooking(String guest, String phone) {
        open("/bookings/new");
        type(By.id("guestName"), guest);
        type(By.id("guestPhone"), phone);
        new Select(driver.findElement(By.id("propertyId"))).selectByIndex(1);
        setDate(By.id("checkIn"), LocalDate.now().plusDays(20).toString());
        setDate(By.id("checkOut"), LocalDate.now().plusDays(22).toString());
        type(By.id("guests"), "2");
        clickAndWaitForPage(By.id("save-btn"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("booking-ref")));
        return text(By.id("booking-ref"));
    }

    @Test
    @DisplayName("Staff creates a booking; it is saved as Pending with a reference")
    void createBooking() {
        loginAs("staff", "staff123");
        String guest = "Selenium Guest " + System.currentTimeMillis();

        String ref = createBooking(guest, "9123456789");

        assertThat(ref).startsWith("HS-");
        assertThat(text(By.id("flash-success"))).contains(ref).contains("created");
        assertThat(text(By.id("detail-guest"))).isEqualTo(guest);
        assertThat(text(By.id("booking-status"))).isEqualTo("Pending");
        assertThat(text(By.id("detail-stay"))).contains("2 night(s)");
    }

    @Test
    @DisplayName("Invalid phone and dates are rejected with field messages")
    void validationErrors() {
        loginAs("host", "host123");
        open("/bookings/new");
        type(By.id("guestName"), "Bad Data Guest");
        type(By.id("guestPhone"), "12345");
        new Select(driver.findElement(By.id("propertyId"))).selectByIndex(1);
        setDate(By.id("checkIn"), LocalDate.now().plusDays(5).toString());
        setDate(By.id("checkOut"), LocalDate.now().plusDays(3).toString());
        clickAndWaitForPage(By.id("save-btn"));

        assertThat(text(By.id("form-errors"))).contains("fix the highlighted fields");
        assertThat(text(By.id("err-guestPhone"))).contains("10 digits");
        assertThat(text(By.id("err-checkOut"))).contains("after check-in");
    }

    @Test
    @DisplayName("Host edits a booking and searches for it by name and status")
    void editAndSearch() {
        loginAs("host", "host123");
        String guest = "Searchable " + System.currentTimeMillis();
        String ref = createBooking(guest, "9000000001");

        click(By.id("edit-btn"));
        type(By.id("guestPhone"), "9000000002");
        clickAndWaitForPage(By.id("save-btn"));
        assertThat(text(By.id("flash-success"))).contains("updated");
        assertThat(text(By.id("detail-phone"))).isEqualTo("9000000002");

        open("/bookings");
        type(By.id("search-q"), guest);
        new Select(driver.findElement(By.id("search-status"))).selectByValue("PENDING");
        clickAndWaitForPage(By.id("search-btn"));

        assertThat(text(By.id("result-count"))).isEqualTo("1 booking(s)");
        assertThat(text(By.cssSelector("#bookings-table .booking-link"))).isEqualTo(ref);

        new Select(driver.findElement(By.id("search-status"))).selectByValue("CANCELLED");
        clickAndWaitForPage(By.id("search-btn"));
        assertThat(text(By.id("no-results"))).contains("No bookings");
    }
}
