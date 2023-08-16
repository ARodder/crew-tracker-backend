package net.aroder.TripTracker.mappers;

import net.aroder.TripTracker.models.*;
import net.aroder.TripTracker.models.DTOs.PaxDTOs.PaxDTO;
import net.aroder.TripTracker.models.DTOs.ShipDTOs.ShipDTO;
import net.aroder.TripTracker.models.DTOs.TripDTOs.TripAdminDTO;
import net.aroder.TripTracker.models.DTOs.TripDTOs.TripDTO;
import net.aroder.TripTracker.models.DTOs.TripDTOs.TripDispatchDTO;
import net.aroder.TripTracker.models.DTOs.TripDTOs.TripOrganizeDTO;
import net.aroder.TripTracker.models.DTOs.UserDTOs.DriverDTO;
import net.aroder.TripTracker.repositories.OrganizerCompanyRepository;
import net.aroder.TripTracker.repositories.RegionRepository;
import net.aroder.TripTracker.services.TripService;
import net.aroder.TripTracker.services.UserService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;

/**
 * Mapper class for converting between Trip entities and DTOs.
 * Uses MapStruct library for mapping operations.
 */
@Mapper(componentModel = "spring")
public abstract class TripMapper {

    @Autowired
    private PaxMapper paxMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private RegionRepository regionRepository;
    @Autowired
    private OrganizerCompanyRepository organizerCompanyRepository;
    @Autowired
    private ShipMapper shipMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TripService tripService;

    /**
     * Converts a Trip entity to a TripDTO.
     *
     * @param trip The Trip entity to be converted.
     * @return The corresponding TripDTO.
     */
    @Mapping(source = "driver",target="driver",qualifiedByName = "driverToDriverDto")
    @Mapping(source = "passengers",target="passengers",qualifiedByName = "paxToPaxDto")
    @Mapping(source = "region",target="regionId",qualifiedByName = "regionToRegionId")
    @Mapping(source = "organizerCompany",target="organizerCompanyId",qualifiedByName = "organizerCompanyToOrganizerCompanyId")
    @Mapping(source = "ship",target="ship",qualifiedByName = "shipToShipDto")
    @Mapping(source = "subContractorPrice",target="price")
    public abstract TripDispatchDTO toTripDTODispatch(Trip trip);

    @Mapping(source = "driver",target="driver",qualifiedByName = "driverToDriverDto")
    @Mapping(source = "passengers",target="passengers",qualifiedByName = "paxToPaxDto")
    @Mapping(source = "region",target="regionId",qualifiedByName = "regionToRegionId")
    @Mapping(source = "organizerCompany",target="organizerCompanyId",qualifiedByName = "organizerCompanyToOrganizerCompanyId")
    @Mapping(source = "ship",target="ship",qualifiedByName = "shipToShipDto")
    @Mapping(source = "externalPrice",target="price")
    public abstract TripOrganizeDTO toTripDTOOrganize(Trip trip);

    @Mapping(source = "driver",target="driver",qualifiedByName = "driverToDriverDto")
    @Mapping(source = "passengers",target="passengers",qualifiedByName = "paxToPaxDto")
    @Mapping(source = "region",target="regionId",qualifiedByName = "regionToRegionId")
    @Mapping(source = "organizerCompany",target="organizerCompanyId",qualifiedByName = "organizerCompanyToOrganizerCompanyId")
    @Mapping(source = "ship",target="ship",qualifiedByName = "shipToShipDto")
    @Mapping(source = "externalPrice",target="externalPrice")
    @Mapping(source = "subContractorPrice",target="subContractorPrice")
    public abstract TripAdminDTO toTripDTOAdmin(Trip trip);

    public List<Trip> toTripById(List<Long> tripIds){
        return tripIds.stream().map(tripId -> tripService.findTripById(tripId)).toList();
    }

    /**
     * Converts a TripDTO to a Trip
     *
     * @param trip tripDTO to convert.
     * @return Trip
     */
    @Mapping(source = "driver",target="driver",qualifiedByName = "driverDtoToDriver")
    @Mapping(source = "passengers",target="passengers",qualifiedByName = "paxDtoToPax")
    @Mapping(source = "regionId",target="region",qualifiedByName = "regionIdToRegion")
    @Mapping(source = "organizerCompanyId",target="organizerCompany",qualifiedByName = "organizerCompanyIdToOrganizerCompany")
    @Mapping(source = "ship",target="ship",qualifiedByName = "shipDtoToShip")
    public abstract Trip toTrip(TripDTO trip);

    public abstract Collection<Trip> toTrip(List<TripDTO> trip);

    @Named("shipDtoToShip")
    public Ship shipDtoToShip(ShipDTO ship){
        return ship != null ? shipMapper.toShip(ship): null;
    }

    @Named("shipToShipDto")
    public ShipDTO shipToShipDto(Ship ship){
        return ship != null ? shipMapper.toShipDto(ship): null;
    }

    @Named("driverDtoToDriver")
    public User driverIdToDriver(DriverDTO driver){
        if(driver == null) return null;
        return userService.findById(driver.getId());
    }

    @Named("paxDtoToPax")
    public List<PAX> paxDtoToPax(List<PaxDTO> paxList){
        return paxList != null ? (List<PAX>) paxMapper.toPax(paxList) : null;
    }

    @Named("regionIdToRegion")
    public Region regionIdToRegion(Long regionId){
        return regionId != null ? regionRepository.findById(regionId).orElse(null): null;
    }
    @Named("organizerCompanyIdToOrganizerCompany")
    public OrganizerCompany organizerCompanyIdToOrganizerCompany(Long companyId){
        return companyId != null ? organizerCompanyRepository.findById(companyId).orElse(null): null;
    }

    /**
     * Converts a list of Trip entities to a list of TripDTOs.
     *
     * @param trip The list of Trip entities to be converted.
     * @return The corresponding list of TripDTOs.
     */
    public abstract List<TripDispatchDTO> toTripDTODispatch(List<Trip> trip);
    public abstract List<TripOrganizeDTO> toTripDTOOrganize(List<Trip> trip);

    /**
     * Converts a list of PAX entities to a list of PaxDTOs using the PaxMapper.
     *
     * @param paxList The list of PAX entities to be converted.
     * @return The corresponding list of PaxDTOs.
     */
    @Named("paxToPaxDto")
    public List<PaxDTO> paxToPaxDto(List<PAX> paxList){
        return paxList != null ? paxMapper.toPaxDTO(paxList) : null;
    }

    /**
     * Converts a User object to its corresponding ID (driver ID).
     *
     * @param driver The User object to be converted.
     * @return The ID of the driver.
     */
    @Named("driverToDriverDto")
    public DriverDTO driverToDriverId(User driver){
        return driver != null ? userMapper.toDriverDTO(driver) : null;
    }

    /**
     * Converts a Region object to its corresponding ID (region ID).
     *
     * @param region The Region object to be converted.
     * @return The ID of the region.
     */
    @Named("regionToRegionId")
    public Long regionToRegionId(Region region){
        return region != null ? region.getId() : null;
    }

    /**
     * Converts an OrganizerCompany object to its corresponding ID (organizer company ID).
     *
     * @param company The OrganizerCompany object to be converted.
     * @return The ID of the organizer company.
     */
    @Named("organizerCompanyToOrganizerCompanyId")
    public Long organizerCompanyToOrganizerCompanyId(OrganizerCompany company){
        return company != null ? company.getId() : null;
    }

}
