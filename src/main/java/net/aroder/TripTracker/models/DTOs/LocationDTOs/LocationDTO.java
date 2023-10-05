package net.aroder.TripTracker.models.DTOs.LocationDTOs;

import jakarta.persistence.*;
import lombok.Data;
import net.aroder.TripTracker.models.Region;

@Data
public class LocationDTO {
    private Long id;
    private String name;
    private Double longitude;
    private Double latitude;
    private Long region;
    private String error;
}
