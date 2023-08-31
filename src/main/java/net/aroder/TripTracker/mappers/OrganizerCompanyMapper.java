package net.aroder.TripTracker.mappers;

import net.aroder.TripTracker.models.DTOs.CompanyDTOs.CompanyDTO;
import net.aroder.TripTracker.models.DTOs.CompanyDTOs.OrganizerCompanyDTO;
import net.aroder.TripTracker.models.OrganizerCompany;
import net.aroder.TripTracker.models.Ship;
import net.aroder.TripTracker.models.User;
import java.util.List;

import net.aroder.TripTracker.services.OrganizerCompanyService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class OrganizerCompanyMapper {
    @Autowired
    private OrganizerCompanyService organizerCompanyService;


    public abstract OrganizerCompanyDTO toOrganizerCompanyDTO(OrganizerCompany organizerCompany);

    public abstract List<OrganizerCompanyDTO> toOrganizerCompanyDTO(List<OrganizerCompany> organizerCompanies);

    @Mapping(source = "employees",target="employeeCount",qualifiedByName = "employeesToEmployeeCount")
    @Mapping(source = "ships", target="domainResourceCount", qualifiedByName = "shipsToDomainResourceCount")
    public abstract CompanyDTO toCompanyDTO(OrganizerCompany organizerCompany);

    public abstract List<CompanyDTO> toCompanyDTO(List<OrganizerCompany> organizerCompanies);

    public OrganizerCompany toOrganizerCompany(CompanyDTO companyDTO){
        return companyDTO != null ? organizerCompanyService.findOrganizerCompanyById(companyDTO.getId()):null;
    }

    public abstract List<OrganizerCompany> toOrganizerCompany(List<CompanyDTO> companyDTO);
    @Named("employeesToEmployeeCount")
    public Long employeesToEmployeeCount(List<User> employees){
        return employees != null ? employees.size() : 0L;
    }
    @Named("shipsToDomainResourceCount")
    public Long shipsToDomainResourceCount(List<Ship> ships){
        return ships != null ? ships.size() : 0L;
    }
}
