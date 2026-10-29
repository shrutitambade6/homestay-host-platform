package com.homestay.platform.web;

import com.homestay.platform.model.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

final class CurrentUser {

    private CurrentUser() {
    }

    static List<Role> roles(Authentication auth) {
        return Role.fromAuthorities(auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
    }
}
