package com.homestay.platform.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/** Journey 4: role-based status workflow across two users. */
class StatusWorkflowIT extends BaseSeleniumIT {

    @Test
    @DisplayName("Host confirms; staff cannot confirm but can check in and check out")
    void roleBasedWorkflow() {
        // Staff creates the booking and sees no Confirm button
        loginAs("staff", "staff123");
        open("/bookings/new");
        type(By.id("guestName"), "Workflow Guest " + System.currentTimeMillis());
        type(By.id("guestPhone"), "9111111111");
        new Select(driver.findElement(By.id("propertyId"))).selectByIndex(1);
        setDate(By.id("checkIn"), LocalDate.now().plusDays(30).toString());
        setDate(By.id("checkOut"), LocalDate.now().plusDays(31).toString());
        clickAndWaitForPage(By.id("save-btn"));
        String bookingUrl = driver.getCurrentUrl();

        assertThat(text(By.id("booking-status"))).isEqualTo("Pending");
        assertThat(driver.findElements(By.id("action-CONFIRM"))).isEmpty();
        assertThat(driver.findElements(By.id("action-CANCEL"))).isEmpty();
        logout();

        // Host confirms
        loginAs("host", "host123");
        driver.get(bookingUrl);
        clickAndWaitForPage(By.id("action-CONFIRM"));
        assertThat(text(By.id("booking-status"))).isEqualTo("Confirmed");
        assertThat(text(By.id("flash-success"))).contains("confirmed");
        logout();

        // Staff checks the guest in and out
        loginAs("staff", "staff123");
        driver.get(bookingUrl);
        clickAndWaitForPage(By.id("action-CHECK_IN"));
        assertThat(text(By.id("booking-status"))).isEqualTo("Checked in");
        assertThat(driver.findElements(By.id("edit-btn"))).isEmpty();

        clickAndWaitForPage(By.id("action-CHECK_OUT"));
        assertThat(text(By.id("booking-status"))).isEqualTo("Checked out");
        assertThat(text(By.id("no-actions"))).contains("No status changes");
        assertThat(text(By.id("detail-updated-by"))).isEqualTo("staff");
    }
}
