package com.homestay.platform.repository;

import com.homestay.platform.model.Booking;
import com.homestay.platform.model.BookingStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
