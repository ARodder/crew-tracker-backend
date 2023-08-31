package net.aroder.TripTracker.mappers;

import net.aroder.TripTracker.models.DTOs.CompanyDTOs.CompanyDTO;
import net.aroder.TripTracker.models.DTOs.CompanyDTOs.DispatcherCompanyDTO;
import net.aroder.TripTracker.models.DispatcherCompany;
import net.aroder.TripTracker.models.Region;
import net.aroder.TripTracker.models.User;
import net.aroder.TripTracker.services.RegionService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class DispatcherCompanyMapper {
    @Autowired
    private RegionService regionService;



    @Mapping(source = "regions",target = "regions",qualifiedByName = "regionToRegionName")
    public abstract DispatcherCompanyDTO toDispatcherCompanyDto(DispatcherCompany dispatcherCompany);

    public abstract List<DispatcherCompanyDTO> toDispatcherCompanyDto(List<DispatcherCompany> dispatcherCompanies);

    @Named("regionToRegionName")
    public List<String> regionToRegionName(List<Region> regions){
        if(regions == null) return null;
        return regions.stream().map(Region::getName).toList();
    }

    @Mapping(source = "drivers",target="employeeCount",qualifiedByName = "driversToEmployeeCount")
    @Mapping(source = "regions", target="domainResourceCount", qualifiedByName = "regionsToDomainResourceCount")
    public abstract CompanyDTO toCompanyDTO(DispatcherCompany dispatcherCompany);

    public abstract List<CompanyDTO> toCompanyDTO(List<DispatcherCompany> dispatcherCompany);

    @Mapping(source = "regions",target = "regions", qualifiedByName = "regionNameToRegion")
    public abstract DispatcherCompany toDispatcherCompany(DispatcherCompanyDTO dispatcherCompanyDTO);

    @Named("regionNameToRegion")
    public List<Region> regionNameToRegion(List<String> regionNames){
        return regionNames != null ? regionNames.stream().map(regionService::findRegionByName).toList(): null;
    }

    @Named("driversToEmployeeCount")
    public Long driversToEmployeeCount(List<User> drivers){
        return drivers != null ? drivers.size() : 0L;
    }
    @Named("regionsToDomainResourceCount")
    public Long regionsToDomainResourceCount(List<Region> regions){
        return regions != null ? regions.size() : 0L;
    }
}
