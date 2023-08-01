package net.aroder.TripTracker.models.DTOs.AuthDTOs;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A class to manage signup data for keycloak signup request.
 */
@Data
@AllArgsConstructor
public class SignupKeycloakRequest {
    private String firstName;
    private String lastName;
    private String email;
    private boolean enabled;
    private boolean emailVerified;
    private List<KeycloakCredentials> credentials;
}
