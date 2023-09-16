package net.aroder.TripTracker.models.DTOs.TripDTOs;

import lombok.Data;
import net.aroder.TripTracker.models.DTOs.PaxDTOs.ManualOrderPaxDTO;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Data
public class ManualTripDTO {
    private String destination;
    private String harbour;
    private String organizerCompany;
    private String passengerRemarks;
    private List<ManualOrderPaxDTO> passengers = new ArrayList<>();
    private String pickUpLocation;
    private Timestamp pickUpTime;
    private String poNumber;
    private String ship;

    public boolean isValid(){
        return destination != null && harbour != null && organizerCompany != null && passengers != null && pickUpLocation != null && pickUpTime != null && poNumber != null && ship != null && passengers.size() > 0;
    }
}
