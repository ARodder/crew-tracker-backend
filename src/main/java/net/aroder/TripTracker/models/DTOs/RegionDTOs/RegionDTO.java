package net.aroder.TripTracker.models.DTOs.RegionDTOs;

import lombok.Data;
import net.aroder.TripTracker.models.DTOs.CompanyDTOs.DispatcherCompanyDTO;
import net.aroder.TripTracker.models.Location;

@Data
public class RegionDTO {
    private Long id;
    private String name;
    private DispatcherCompanyDTO dispatcherCompany;
    private Location regionLocation;
}
