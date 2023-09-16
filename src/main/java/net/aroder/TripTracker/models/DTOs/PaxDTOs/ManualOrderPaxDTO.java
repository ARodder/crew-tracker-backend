package net.aroder.TripTracker.models.DTOs.PaxDTOs;

import lombok.Data;

@Data
public class ManualOrderPaxDTO {
    private String firstName;
    private String surname;
    private boolean immigration;
    private String organization;
}
