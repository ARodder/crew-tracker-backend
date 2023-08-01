package net.aroder.TripTracker.models.DTOs;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

@Data
public class DispatchSearchObject {
    private Integer pageNum;
    private Boolean hideAssignedTrips;
    private Boolean hideCanceledTrips;
    private Boolean showPastTrips;
    private Boolean hidePricedTrips;
    private Long poNumber;
    private String region;
}
