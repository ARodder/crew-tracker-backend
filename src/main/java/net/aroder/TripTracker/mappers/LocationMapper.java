package net.aroder.TripTracker.mappers;

import net.aroder.TripTracker.models.DTOs.LocationDTOs.LocationDTO;
import net.aroder.TripTracker.models.Location;
import net.aroder.TripTracker.models.Region;
import net.aroder.TripTracker.services.LocationService;
import net.aroder.TripTracker.services.RegionService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class LocationMapper {

    @Autowired
    private RegionService regionService;
    @Autowired
    private LocationService locationService;

    @Mapping(source="region",target = "region",qualifiedByName = "toRegionId")
    public abstract LocationDTO toLocationDTO(Location location);

    public abstract List<LocationDTO> toLocationDTO(List<Location> location);

    @Mapping(source="region",target = "region",qualifiedByName = "toRegion")
    public abstract Location toLocation(LocationDTO locationDTO);

    public Location toLocationById(Long locationId){
        return locationId != null ? locationService.findLocationById(locationId):null;
    }

    @Named("toRegion")
    public Region toRegion(Long regionId){
        return regionId != null ? regionService.findRegionById(regionId):null;
    }
    @Named("toRegionId")
    public Long toRegionId(Region region){
        return region != null ? region.getId():null;
    }
}
