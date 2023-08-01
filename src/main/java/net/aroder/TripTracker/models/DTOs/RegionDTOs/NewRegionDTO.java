package net.aroder.TripTracker.models.DTOs.RegionDTOs;


import lombok.Data;

@Data
public class NewRegionDTO {
    private Long id;
    private String name;
    private String dispatcherCompanyName;
    private String regionLocationName;
}
