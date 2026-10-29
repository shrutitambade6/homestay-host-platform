package com.homestay.platform.service;

import com.homestay.platform.model.Booking;
import com.homestay.platform.model.BookingStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Numbers shown on the summary dashboard. */
public record DashboardSummary(
        long totalBookings,
        Map<BookingStatus, Long> countsByStatus,
        long todaysCheckIns,
        long guestsInHouse,
        BigDecimal revenue,
        long activeProperties,
        long totalRooms,
        long roomsOccupiedTonight,
        int occupancyPercent,
        List<Booking> upcoming) {
}
