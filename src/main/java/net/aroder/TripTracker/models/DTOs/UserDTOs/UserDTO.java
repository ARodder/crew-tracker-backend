package net.aroder.TripTracker.models.DTOs.UserDTOs;

import lombok.Data;
import net.aroder.TripTracker.models.DTOs.CompanyDTOs.DispatcherCompanyDTO;
import net.aroder.TripTracker.models.DTOs.CompanyDTOs.OrganizerCompanyDTO;

import java.util.List;

/**
 * A data class representing a User DTO.
 */
@Data
public class UserDTO {
    private String id;
    private String firstName;
    private String surname;
    private String email;
    private boolean tosAccepted;
    private List<String> roles;
    private OrganizerCompanyDTO organizerCompany;
    private DispatcherCompanyDTO dispatcherCompany;
    private String phoneNumber;


}
