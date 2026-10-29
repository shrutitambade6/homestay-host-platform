package com.homestay.platform.model;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

/**
 * Role-based status workflow.
 *
 * <pre>
 *   PENDING --CONFIRM(host,admin)--> CONFIRMED --CHECK_IN(staff,host,admin)--> CHECKED_IN
 *   CHECKED_IN --CHECK_OUT(staff,host,admin)--> CHECKED_OUT
 *   PENDING | CONFIRMED --CANCEL(host,admin)--> CANCELLED
 * </pre>
 */
public enum BookingAction {
    CONFIRM("Confirm", EnumSet.of(BookingStatus.PENDING), BookingStatus.CONFIRMED,
            Set.of(Role.HOST, Role.ADMIN)),
    CHECK_IN("Check in", EnumSet.of(BookingStatus.CONFIRMED), BookingStatus.CHECKED_IN,
            Set.of(Role.STAFF, Role.HOST, Role.ADMIN)),
    CHECK_OUT("Check out", EnumSet.of(BookingStatus.CHECKED_IN), BookingStatus.CHECKED_OUT,
            Set.of(Role.STAFF, Role.HOST, Role.ADMIN)),
    CANCEL("Cancel", EnumSet.of(BookingStatus.PENDING, BookingStatus.CONFIRMED), BookingStatus.CANCELLED,
            Set.of(Role.HOST, Role.ADMIN));

    private final String label;
    private final Set<BookingStatus> from;
    private final BookingStatus to;
    private final Set<Role> allowedRoles;

    BookingAction(String label, Set<BookingStatus> from, BookingStatus to, Set<Role> allowedRoles) {
        this.label = label;
        this.from = from;
        this.to = to;
        this.allowedRoles = allowedRoles;
    }

    public String getLabel() {
        return label;
    }

    public BookingStatus getTo() {
        return to;
    }

    public boolean canApplyTo(BookingStatus current) {
        return from.contains(current);
    }

    public boolean isAllowedFor(Collection<Role> roles) {
        return roles.stream().anyMatch(allowedRoles::contains);
    }

    public boolean isAvailable(BookingStatus current, Collection<Role> roles) {
        return canApplyTo(current) && isAllowedFor(roles);
    }
}
