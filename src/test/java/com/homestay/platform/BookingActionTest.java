package com.homestay.platform;

import com.homestay.platform.model.BookingAction;
import com.homestay.platform.model.BookingStatus;
import com.homestay.platform.model.Role;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Pure unit tests for the role-based status workflow rules. */
class BookingActionTest {

    @Test
    void hostCanConfirmPendingBooking() {
        assertThat(BookingAction.CONFIRM.isAvailable(BookingStatus.PENDING, List.of(Role.HOST))).isTrue();
        assertThat(BookingAction.CONFIRM.getTo()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void staffCannotConfirmOrCancel() {
        assertThat(BookingAction.CONFIRM.isAllowedFor(List.of(Role.STAFF))).isFalse();
        assertThat(BookingAction.CANCEL.isAllowedFor(List.of(Role.STAFF))).isFalse();
    }

    @Test
    void staffCanCheckInConfirmedAndCheckOutCheckedIn() {
        assertThat(BookingAction.CHECK_IN.isAvailable(BookingStatus.CONFIRMED, List.of(Role.STAFF))).isTrue();
        assertThat(BookingAction.CHECK_OUT.isAvailable(BookingStatus.CHECKED_IN, List.of(Role.STAFF))).isTrue();
    }

    @Test
    void cannotSkipSteps() {
        assertThat(BookingAction.CHECK_IN.canApplyTo(BookingStatus.PENDING)).isFalse();
        assertThat(BookingAction.CHECK_OUT.canApplyTo(BookingStatus.CONFIRMED)).isFalse();
    }

    @Test
    void finishedBookingsCannotBeCancelled() {
        assertThat(BookingAction.CANCEL.canApplyTo(BookingStatus.CHECKED_IN)).isFalse();
        assertThat(BookingAction.CANCEL.canApplyTo(BookingStatus.CHECKED_OUT)).isFalse();
        assertThat(BookingAction.CANCEL.canApplyTo(BookingStatus.CANCELLED)).isFalse();
    }

    @Test
    void onlyPendingAndConfirmedAreEditable() {
        assertThat(BookingStatus.PENDING.isEditable()).isTrue();
        assertThat(BookingStatus.CONFIRMED.isEditable()).isTrue();
        assertThat(BookingStatus.CHECKED_IN.isEditable()).isFalse();
    }
}
