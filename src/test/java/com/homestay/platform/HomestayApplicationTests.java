package com.homestay.platform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class HomestayApplicationTests {

    @Test
    void contextLoads() {
        // Fails if the Spring context (security, JPA, web) cannot start
    }
}
