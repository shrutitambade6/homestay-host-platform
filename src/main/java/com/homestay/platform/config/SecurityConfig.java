package com.homestay.platform.config;

import com.homestay.platform.model.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

/**
 * Three demo roles (MVP scope):
 *  ADMIN  - everything
 *  HOST   - manage properties, confirm / cancel bookings
 *  STAFF  - create bookings, check guests in and out
 * Passwords can be overridden with the properties app.users.admin-password / host-password / staff-password
 * (environment variables APP_USERS_ADMINPASSWORD, APP_USERS_HOSTPASSWORD, APP_USERS_STAFFPASSWORD).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Explicit ant matchers: safe on external Tomcat, which registers extra servlets
                .requestMatchers(antMatcher("/login"), antMatcher("/css/**"), antMatcher("/js/**"), antMatcher("/img/**"),
                        antMatcher("/favicon.ico"), antMatcher("/error")).permitAll()
                .requestMatchers(antMatcher("/actuator/health/**"), antMatcher("/actuator/info"),
                        antMatcher("/api/version")).permitAll()
                .requestMatchers(antMatcher("/properties/new"), antMatcher("/properties/*/edit"))
                        .hasAnyRole("HOST", "ADMIN")
                .requestMatchers(antMatcher(HttpMethod.POST, "/properties/**")).hasAnyRole("HOST", "ADMIN")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error")
                .permitAll())
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll())
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService users(PasswordEncoder encoder,
                                    @Value("${app.users.admin-password:admin123}") String adminPassword,
                                    @Value("${app.users.host-password:host123}") String hostPassword,
                                    @Value("${app.users.staff-password:staff123}") String staffPassword) {
        return new InMemoryUserDetailsManager(
                User.withUsername("admin").password(encoder.encode(adminPassword)).roles(Role.ADMIN.name()).build(),
                User.withUsername("host").password(encoder.encode(hostPassword)).roles(Role.HOST.name()).build(),
                User.withUsername("staff").password(encoder.encode(staffPassword)).roles(Role.STAFF.name()).build());
    }
}
