package com.homestay.platform.repository;

import com.homestay.platform.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    List<Property> findAllByOrderByNameAsc();

    List<Property> findByActiveTrueOrderByNameAsc();

    long countByActiveTrue();

    @Query("select coalesce(sum(p.totalRooms), 0) from Property p where p.active = true")
    Long totalActiveRooms();
}
