package net.aroder.TripTracker.models.DTOs.ShipDTOs;

import lombok.Data;

@Data
public class ShipDTO {
    private Long id;
    private String name;
    private Long imo;
    private Long organizerCompanyId;
}
