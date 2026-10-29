package com.homestay.platform.web.dto;

import com.homestay.platform.model.Property;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class PropertyForm {

    @NotBlank(message = "Property name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Location is required")
    @Size(max = 120)
    private String location;

    @Min(value = 1, message = "At least 1 room")
    @Max(value = 100, message = "At most 100 rooms")
    private int totalRooms = 1;

    @NotNull(message = "Price per night is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal pricePerNight;

    private boolean active = true;

    public static PropertyForm from(Property p) {
        PropertyForm f = new PropertyForm();
        f.name = p.getName();
        f.location = p.getLocation();
        f.totalRooms = p.getTotalRooms();
        f.pricePerNight = p.getPricePerNight();
        f.active = p.isActive();
        return f;
    }

    public void applyTo(Property p) {
        p.setName(name.trim());
        p.setLocation(location.trim());
        p.setTotalRooms(totalRooms);
        p.setPricePerNight(pricePerNight);
        p.setActive(active);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public int getTotalRooms() { return totalRooms; }
    public void setTotalRooms(int totalRooms) { this.totalRooms = totalRooms; }
    public BigDecimal getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
