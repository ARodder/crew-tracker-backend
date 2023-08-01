package net.aroder.TripTracker.models.DTOs.AuthDTOs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDetails {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private boolean emailVerified;
    private boolean enabled;
    private String createdTimestamp;

}
