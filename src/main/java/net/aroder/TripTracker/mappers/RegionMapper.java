package net.aroder.TripTracker.mappers;

import net.aroder.TripTracker.models.DTOs.CompanyDTOs.DispatcherCompanyDTO;
import net.aroder.TripTracker.models.DTOs.RegionDTOs.RegionDTO;
import net.aroder.TripTracker.models.DispatcherCompany;
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


    @Mapping(source = "dispatcherCompany",target="dispatcherCompany", qualifiedByName = "dispatcherCompanyToDispatcherCompanyDTO")
    public abstract RegionDTO toRegionDto(Region region);

    public abstract List<RegionDTO> toRegionDto(List<Region> region);

    @Named("dispatcherCompanyToDispatcherCompanyDTO")
    public DispatcherCompanyDTO dispatcherCompanyToDispatcherCompanyDTO(DispatcherCompany dispatcherCompany){
        return dispatcherCompany != null ? dispatcherCompanyMapper.toDispatcherCompanyDto(dispatcherCompany) : null;
    }

    @Mapping(source = "dispatcherCompany",target="dispatcherCompany", qualifiedByName = "dispatcherCompanyDTOToDispatcherCompany")
    public abstract Region regionDtoToRegion(RegionDTO regionDTO);

    @Named("dispatcherCompanyDTOToDispatcherCompany")
    public DispatcherCompany dispatcherCompanyDTOToDispatcherCompany(DispatcherCompanyDTO dispatcherCompanyDTO){
        return dispatcherCompanyDTO != null ? dispatcherCompanyMapper.toDispatcherCompany(dispatcherCompanyDTO) : null;
    }



}
