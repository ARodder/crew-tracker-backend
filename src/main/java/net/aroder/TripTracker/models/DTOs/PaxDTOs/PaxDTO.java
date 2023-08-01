package net.aroder.TripTracker.models.DTOs.PaxDTOs;

import lombok.Data;
import java.sql.Timestamp;

/**
 * A data class representing a Passenger (Pax) DTO.
 */
@Data
public class PaxDTO {

        private Long id;
        private String status;
        private String firstName;
        private String surname;
        private String flight;
        private Timestamp pickUpTime;
        private Timestamp expirationDate;
        private String pickUpLocation;
        private String destination;
        private boolean immigration;
        private String organization;
        private String remarks;
        private Long poNumber;
        private boolean paxValid;
        private Long harbourId;
        private Long shipId;
        private Long tripId;
        private String error;
        private Double cost;
}
