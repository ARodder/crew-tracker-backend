package net.aroder.TripTracker.models.DTOs.TripDTOs;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.aroder.TripTracker.models.DTOs.PaxDTOs.PaxDTO;
import net.aroder.TripTracker.models.DTOs.ShipDTOs.ShipDTO;
import net.aroder.TripTracker.models.DTOs.UserDTOs.DriverDTO;
import net.aroder.TripTracker.models.Location;

import java.sql.Timestamp;
import java.util.List;

/**
 * A data class representing a Trip DTO.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TripDispatchDTO extends TripDTO{
    private Double price;
}
