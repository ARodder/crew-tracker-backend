package net.aroder.TripTracker.models.DTOs.TripDTOs;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * A data class representing a Trip DTO.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TripAdminDTO extends TripDTO{

    private Double subContractorPrice;
    private Double externalPrice;
}
