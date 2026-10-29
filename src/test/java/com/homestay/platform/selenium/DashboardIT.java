package com.homestay.platform.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.assertj.core.api.Assertions.assertThat;

/** Journey 5: summary dashboard and role-restricted property management. */
class DashboardIT extends BaseSeleniumIT {

    @Test
    @DisplayName("Dashboard shows summary tiles and status drill-down works")
    void dashboardSummary() {
        loginAs("admin", "admin123");

        int total = Integer.parseInt(text(By.cssSelector("#stat-total .value")));
        assertThat(total).isGreaterThanOrEqualTo(6);
        assertThat(text(By.cssSelector("#stat-revenue .value"))).startsWith("₹");
        assertThat(text(By.cssSelector("#stat-occupancy .value"))).endsWith("%");
        assertThat(driver.findElement(By.id("app-version")).getText()).startsWith("v");

        String pending = text(By.id("count-PENDING"));
        click(By.id("count-PENDING"));
        assertThat(text(By.id("result-count"))).isEqualTo(pending + " booking(s)");
    }

    @Test
    @DisplayName("Only host/admin can add properties")
    void propertyPermissions() {
        loginAs("staff", "staff123");
        click(By.id("nav-properties"));
        assertThat(driver.findElements(By.id("new-property-btn"))).isEmpty();
        logout();

        loginAs("host", "host123");
        click(By.id("nav-properties"));
        click(By.id("new-property-btn"));
        String name = "Selenium Retreat " + System.currentTimeMillis();
        type(By.id("name"), name);
        type(By.id("location"), "Nashik");
        type(By.id("totalRooms"), "2");
        type(By.id("pricePerNight"), "1999");
        clickAndWaitForPage(By.id("save-property-btn"));

        assertThat(text(By.id("flash-success"))).contains(name);
        assertThat(driver.findElements(By.xpath("//td[@class='property-name' and text()='" + name + "']"))).hasSize(1);
    }
}
