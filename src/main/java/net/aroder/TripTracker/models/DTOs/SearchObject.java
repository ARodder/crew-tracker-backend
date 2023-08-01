package net.aroder.TripTracker.models.DTOs;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class SearchObject {
    private String shipName;
    private Timestamp date;
    private Long poNumber;
    private String harbour;
    private Boolean hideCanceledTrips;
    private Boolean archiveMode;

}
