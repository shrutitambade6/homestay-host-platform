package com.homestay.platform.service;

import com.homestay.platform.model.Property;
import com.homestay.platform.repository.PropertyRepository;
import com.homestay.platform.web.dto.PropertyForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PropertyService {

    private final PropertyRepository properties;

    public PropertyService(PropertyRepository properties) {
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public List<Property> findAll() {
        return properties.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public List<Property> findActive() {
        return properties.findByActiveTrueOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Property get(Long id) {
        return properties.findById(id)
                .orElseThrow(() -> new NotFoundException("Property " + id + " not found"));
    }

    public Property create(PropertyForm form) {
        Property p = new Property();
        form.applyTo(p);
        return properties.save(p);
    }

    public Property update(Long id, PropertyForm form) {
        Property p = get(id);
        form.applyTo(p);
        return properties.save(p);
    }
}
