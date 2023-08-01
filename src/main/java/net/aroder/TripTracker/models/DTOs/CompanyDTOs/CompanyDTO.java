package net.aroder.TripTracker.models.DTOs.CompanyDTOs;

import lombok.Data;

@Data
public class CompanyDTO {
    private Long id;
    private String name;
    private Long employeeCount;
    private Long domainResourceCount;
}
