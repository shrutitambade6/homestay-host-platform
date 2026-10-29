package com.homestay.platform.model;

/** Lifecycle of a booking. See BookingAction for the allowed transitions. */
public enum BookingStatus {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    CHECKED_IN("Checked in"),
    CHECKED_OUT("Checked out"),
    CANCELLED("Cancelled");

    private final String label;

    BookingStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Bookings can only be edited before the guest arrives. */
    public boolean isEditable() {
        return this == PENDING || this == CONFIRMED;
    }
}
