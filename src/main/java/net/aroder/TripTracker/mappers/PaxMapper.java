package net.aroder.TripTracker.mappers;

import net.aroder.TripTracker.models.DTOs.PaxDTOs.PaxDTO;
import net.aroder.TripTracker.models.Location;
import net.aroder.TripTracker.models.PAX;
import net.aroder.TripTracker.models.Ship;
import net.aroder.TripTracker.models.Trip;
import net.aroder.TripTracker.repositories.LocationRepository;
import net.aroder.TripTracker.repositories.ShipRepository;
import net.aroder.TripTracker.repositories.TripRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Mapper class for converting between PAX entities and DTOs.
 * Uses MapStruct library for mapping operations.
 */
@Mapper(componentModel = "spring")
public abstract class PaxMapper {
    @Autowired
    private ShipRepository shipRepository;
    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private LocationRepository locationRepository;


    /**
     * Converts a PAX entity to a PaxDTO.
     *
     * @param pax The PAX entity to be converted.
     * @return The corresponding PaxDTO.
     */
    @Mapping(source = "ship",target="shipId",qualifiedByName = "shipToShipId")
    @Mapping(source = "trip",target="tripId",qualifiedByName = "tripToTripId")
    @Mapping(source = "harbour",target="harbourId",qualifiedByName = "harbourToHarbourId")
    public abstract PaxDTO toPaxDTO(PAX pax);

    /**
     * Converts a PaxDTO to a PAX entity.
     *
     * @param pax The PaxDTO to be converted.
     * @return The corresponding PAX entity.
     */
    @Mapping(source = "shipId",target="ship",qualifiedByName = "shipIdToShip")
    @Mapping(source = "tripId",target="trip",qualifiedByName = "tripIdToTrip")
    @Mapping(source = "harbourId",target="harbour",qualifiedByName = "harbourIdToHarbour")
    public abstract PAX toPax(PaxDTO pax);

    /**
     * Converts a collection of PaxDTO objects to a collection of PAX entities.
     *
     * @param pax The collection of PaxDTO objects to be converted.
     * @return The corresponding collection of PAX entities.
     */
    public abstract Collection<PAX> toPax(Collection<PaxDTO> pax);

    /**
     * Converts a list of PAX entities to a list of PaxDTO objects.
     *
     * @param pax The list of PAX entities to be converted.
     * @return The corresponding list of PaxDTO objects.
     */
    public abstract List<PaxDTO> toPaxDTO(List<PAX> pax);


    /**
     * Converts a Location object to its corresponding ID (harbour ID).
     *
     * @param harbour The Location object to be converted.
     * @return The ID of the harbour.
     */
    @Named("harbourToHarbourId")
    public Long harbourToHarbourId(Location harbour){
        return harbour != null ? harbour.getId() : null;
    }

    /**
     * Converts a harbour ID to its corresponding Location object.
     *
     * @param locationId The ID of the harbour.
     * @return The Location object representing the harbour.
     * @throws NoSuchElementException if the Location object is not found.
     */
    @Named("harbourIdToHarbour")
    public Location harbourIdToHarbour(Long locationId){
        if(locationId == null) return null;
        return locationRepository.findById(locationId).orElseThrow(NoSuchElementException::new);
    }

    /**
     * Converts a trip ID to its corresponding Trip object.
     *
     * @param tripId The ID of the trip.
     * @return The Trip object representing the trip.
     * @throws NoSuchElementException if the Trip object is not found.
     */
    @Named("tripIdToTrip")
    public Trip tripIdToTrip(Long tripId){
        if(tripId == null){
            return null;
        }
        return tripRepository.findById(tripId).orElseThrow(NoSuchElementException::new);
    }

    /**
     * Converts a ship ID to its corresponding Ship object.
     *
     * @param shipId The ID of the ship.
     * @return The Ship object representing the ship.
     * @throws NoSuchElementException if the Ship object is not found.
     */
    @Named("shipIdToShip")
    public Ship shipIdToShip(Long shipId){
        if(shipId == null){
            return null;
        }
        return shipRepository.findById(shipId).orElseThrow(NoSuchElementException::new);
    }


    /**
     * Converts a Ship object to its corresponding ID (ship ID).
     *
     * @param ship The Ship object to be converted.
     * @return The ID of the ship.
     */
    @Named("shipToShipId")
    public Long shipToShipId(Ship ship){
        return ship != null ? ship.getId() : null;
    }

    /**
     * Converts a Trip object to its corresponding ID (trip ID).
     *
     * @param trip The Trip object to be converted.
     * @return The ID of the trip.
     */
    @Named("tripToTripId")
    public Long tripToTripId(Trip trip){
        return trip != null ? trip.getId() : null;
    }
}
