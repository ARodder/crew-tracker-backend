package net.aroder.TripTracker.models.DTOs;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class UnreportedTripSearchObject {
    private Integer poNumber;
    private String harbor;
    private String ship;
    private Timestamp startDate;
    private Timestamp endDate;
}
