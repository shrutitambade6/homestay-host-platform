package com.homestay.platform.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.assertj.core.api.Assertions.assertThat;

/** Journey 1: authentication. */
class LoginJourneyIT extends BaseSeleniumIT {

    @Test
    @DisplayName("Host logs in, lands on the dashboard and logs out")
    void validLoginShowsDashboard() {
        loginAs("host", "host123");

        assertThat(text(By.id("page-title"))).isEqualTo("Dashboard");
        assertThat(text(By.id("current-user"))).isEqualTo("host");
        assertThat(text(By.id("current-role"))).isEqualTo("HOST");

        logout();
        assertThat(driver.getCurrentUrl()).contains("/login");
    }

    @Test
    @DisplayName("Wrong password shows an error and stays on login")
    void invalidLoginShowsError() {
        open("/login");
        type(By.id("username"), "host");
        type(By.id("password"), "wrong-password");
        clickAndWaitForPage(By.id("login-btn"));

        assertThat(text(By.id("login-error"))).contains("Invalid username or password");
        assertThat(driver.getCurrentUrl()).contains("/login?error");
    }

    @Test
    @DisplayName("Protected pages redirect anonymous users to login")
    void protectedPageRedirects() {
        open("/bookings");
        assertThat(driver.getCurrentUrl()).contains("/login");
    }
}
