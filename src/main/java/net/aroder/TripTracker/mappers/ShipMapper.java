package net.aroder.TripTracker.mappers;

import net.aroder.TripTracker.models.DTOs.ShipDTOs.ShipDTO;
import net.aroder.TripTracker.models.OrganizerCompany;
import net.aroder.TripTracker.models.Ship;
import net.aroder.TripTracker.repositories.OrganizerCompanyRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class ShipMapper {
    @Autowired
    private OrganizerCompanyRepository organizerCompanyRepository;


    @Mapping(source = "organizerCompany",target = "organizerCompanyId",qualifiedByName = "organizerCompanyToOrganizerCompanyId")
    public abstract ShipDTO toShipDto(Ship ship);

    public abstract List<ShipDTO> toShipDto(List<Ship> shipList);

    @Mapping(source = "organizerCompanyId",target = "organizerCompany",qualifiedByName = "organizerCompanyIdToOrganizerCompany")
    public abstract Ship toShip(ShipDTO shipDto);
    public abstract List<Ship> toShip(List<ShipDTO> shipList);

    @Named("organizerCompanyIdToOrganizerCompany")
    public OrganizerCompany organizerCompanyIdToOrganizerCompany(Long organizerCompanyId){
        return organizerCompanyId != null ? organizerCompanyRepository.findById(organizerCompanyId).orElse(null) : null;
    }

    @Named("organizerCompanyToOrganizerCompanyId")
    public Long organizerCompanyToOrganizerCompanyId(OrganizerCompany organizerCompany){
        return organizerCompany != null ? organizerCompany.getId() : null;
    }
}
