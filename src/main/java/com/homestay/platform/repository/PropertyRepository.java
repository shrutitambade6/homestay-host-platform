package com.homestay.platform.repository;

import com.homestay.platform.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    List<Property> findAllByOrderByNameAsc();

    List<Property> findByActiveTrueOrderByNameAsc();
}
