package com.homestay.platform;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebSecurityTest {

    @Autowired
    MockMvc mvc;

    @Test
    void anonymousUserIsRedirectedToLogin() throws Exception {
        // A browser (Accept: text/html) is sent to the login page; API clients get 401 instead
        mvc.perform(get("/bookings").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void healthAndVersionArePublic() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
        mvc.perform(get("/api/version")).andExpect(status().isOk())
                .andExpect(jsonPath("$.application").value("homestay-host-platform"));
    }

    @Test
    @WithMockUser(username = "host", roles = "HOST")
    void hostSeesBookings() throws Exception {
        mvc.perform(get("/bookings")).andExpect(status().isOk())
                .andExpect(content().string(containsString("bookings-table")));
    }

    @Test
    @WithMockUser(username = "staff", roles = "STAFF")
    void staffCannotAddProperties() throws Exception {
        mvc.perform(get("/properties/new")).andExpect(status().isForbidden());
        mvc.perform(post("/properties").with(csrf())
                        .param("name", "X").param("location", "Y")
                        .param("totalRooms", "1").param("pricePerNight", "100"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "host", roles = "HOST")
    void invalidBookingShowsValidationErrors() throws Exception {
        mvc.perform(post("/bookings").with(csrf())
                        .param("guestName", "")
                        .param("guestPhone", "123")
                        .param("guests", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Please fix the highlighted fields")))
                .andExpect(content().string(containsString("Phone must be exactly 10 digits")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void apiListsBookings() throws Exception {
        mvc.perform(get("/api/bookings")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookingRef").exists());
    }
}
