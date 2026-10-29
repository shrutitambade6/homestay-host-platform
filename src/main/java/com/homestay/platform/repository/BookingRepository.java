package com.homestay.platform.repository;

import com.homestay.platform.model.Booking;
import com.homestay.platform.model.BookingStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Search by free text (guest name, phone or booking reference), optional status and optional property.
     * An empty q, a null status or a null propertyId is ignored.
     */
    @Query("""
            select b from Booking b
            where (:q = ''
                   or lower(b.guestName) like lower(concat('%', :q, '%'))
                   or b.guestPhone like concat('%', :q, '%')
                   or lower(b.bookingRef) like lower(concat('%', :q, '%')))
              and (:status is null or b.status = :status)
              and (:propertyId is null or b.property.id = :propertyId)
            order by b.checkIn desc, b.id desc
            """)
    List<Booking> search(@Param("q") String q,
                         @Param("status") BookingStatus status,
                         @Param("propertyId") Long propertyId);

    long countByStatus(BookingStatus status);

    /* Explicit queries: in derived method names "CheckIn" would be parsed as property "check" + keyword "In". */
    @Query("select count(b) from Booking b where b.checkIn = :day and b.status in :statuses")
    long countArrivals(@Param("day") LocalDate day, @Param("statuses") Collection<BookingStatus> statuses);

    @Query("select coalesce(sum(b.totalAmount), 0) from Booking b where b.status in :statuses")
    BigDecimal sumAmountByStatusIn(@Param("statuses") Collection<BookingStatus> statuses);

    /** Bookings that occupy a room on the given night. */
    @Query("""
            select count(b) from Booking b
            where b.checkIn <= :day and b.checkOut > :day and b.status in :statuses
            """)
    long countOccupying(@Param("day") LocalDate day, @Param("statuses") Collection<BookingStatus> statuses);

    @Query("select b from Booking b where b.checkIn >= :from and b.status in :statuses order by b.checkIn asc, b.id asc")
    List<Booking> findUpcoming(@Param("from") LocalDate from, @Param("statuses") Collection<BookingStatus> statuses,
                               Pageable pageable);
}
