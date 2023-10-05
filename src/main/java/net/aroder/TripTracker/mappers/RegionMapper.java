package net.aroder.TripTracker.mappers;

import net.aroder.TripTracker.models.DTOs.CompanyDTOs.DispatcherCompanyDTO;
import net.aroder.TripTracker.models.DTOs.LocationDTOs.LocationDTO;
import net.aroder.TripTracker.models.DTOs.RegionDTOs.RegionDTO;
import net.aroder.TripTracker.models.DispatcherCompany;
import net.aroder.TripTracker.models.Location;
import net.aroder.TripTracker.models.Region;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class RegionMapper {

    @Autowired
    private DispatcherCompanyMapper dispatcherCompanyMapper;
    @Autowired
    private LocationMapper locationMapper;


    @Mapping(source = "dispatcherCompany",target="dispatcherCompany", qualifiedByName = "dispatcherCompanyToDispatcherCompanyDTO")
    @Mapping(source = "regionLocation",target="regionLocation", qualifiedByName = "locationToLocationDTO")
    public abstract RegionDTO toRegionDto(Region region);

    public abstract List<RegionDTO> toRegionDto(List<Region> region);

    @Named("dispatcherCompanyToDispatcherCompanyDTO")
    public DispatcherCompanyDTO dispatcherCompanyToDispatcherCompanyDTO(DispatcherCompany dispatcherCompany){
        return dispatcherCompany != null ? dispatcherCompanyMapper.toDispatcherCompanyDto(dispatcherCompany) : null;
    }

    @Mapping(source = "dispatcherCompany",target="dispatcherCompany", qualifiedByName = "dispatcherCompanyDTOToDispatcherCompany")
    @Mapping(source = "regionLocation",target="regionLocation", qualifiedByName = "locationDtoToLocation")
    public abstract Region regionDtoToRegion(RegionDTO regionDTO);

    @Named("dispatcherCompanyDTOToDispatcherCompany")
    public DispatcherCompany dispatcherCompanyDTOToDispatcherCompany(DispatcherCompanyDTO dispatcherCompanyDTO){
        return dispatcherCompanyDTO != null ? dispatcherCompanyMapper.toDispatcherCompany(dispatcherCompanyDTO) : null;
    }
    @Named("locationToLocationDTO")
    public LocationDTO locationToLocationDTO(Location location){
        return location != null ? locationMapper.toLocationDTO(location) : null;
    }

    @Named("locationDtoToLocation")
    public Location locationDtoToLocation(LocationDTO locationDTO){
        return locationDTO != null ? locationMapper.toLocation(locationDTO) : null;
    }



}
