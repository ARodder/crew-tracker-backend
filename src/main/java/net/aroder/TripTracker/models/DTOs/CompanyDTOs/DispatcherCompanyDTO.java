package net.aroder.TripTracker.models.DTOs.CompanyDTOs;

import lombok.Data;

import java.util.List;

@Data
public class DispatcherCompanyDTO {
    private Long id;
    private String name;
    private List<String> regions;
}
