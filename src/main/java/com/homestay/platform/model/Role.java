package com.homestay.platform.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Application roles. Spring Security authorities are "ROLE_" + name(). */
public enum Role {
    ADMIN, HOST, STAFF;

    public String authority() {
        return "ROLE_" + name();
    }

    public static Optional<Role> fromAuthority(String authority) {
        return Arrays.stream(values()).filter(r -> r.authority().equals(authority)).findFirst();
    }

    public static List<Role> fromAuthorities(Collection<String> authorities) {
        return authorities.stream().map(Role::fromAuthority).flatMap(Optional::stream).toList();
    }
}
