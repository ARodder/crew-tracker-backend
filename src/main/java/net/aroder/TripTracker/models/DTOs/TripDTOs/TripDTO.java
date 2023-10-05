package net.aroder.TripTracker.models.DTOs.TripDTOs;

import lombok.Data;
import net.aroder.TripTracker.models.DTOs.LocationDTOs.LocationDTO;
import net.aroder.TripTracker.models.DTOs.PaxDTOs.PaxDTO;
import net.aroder.TripTracker.models.DTOs.ShipDTOs.ShipDTO;
import net.aroder.TripTracker.models.DTOs.UserDTOs.DriverDTO;
import net.aroder.TripTracker.models.Location;

import java.sql.Timestamp;
import java.util.List;

/**
 * A data class representing a Trip DTO.
 */
@Data
public abstract class TripDTO {

    private Long id;
    private DriverDTO driver;
    private Timestamp pickUpTime;
    private List<PaxDTO> passengers;
    private LocationDTO pickUpLocation;
    private LocationDTO destination;
    private Boolean immigration;
    private Long regionId;
    private Long poNumber;
    private String driverRemarks;
    private String passengerRemarks;
    private Long organizerCompanyId;
    private String status;
    private ShipDTO ship;
}
