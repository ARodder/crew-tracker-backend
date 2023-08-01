package net.aroder.TripTracker.models.DTOs.DomainFileDTOs;

import lombok.Data;
import net.aroder.TripTracker.models.DTOs.UserDTOs.SimpleUserDTO;

import java.sql.Timestamp;

@Data
public class DomainFileDTO {

    private Long id;
    private String name;
    private Long size;
    private String type;
    private Timestamp createdAt;
    private Long poNumber;
    private String classification;
    private SimpleUserDTO createdBy;
}
