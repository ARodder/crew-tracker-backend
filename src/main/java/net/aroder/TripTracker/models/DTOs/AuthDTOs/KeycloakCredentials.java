package net.aroder.TripTracker.models.DTOs.AuthDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A class for managing credentials used in keycloak.
 */
@Data
@AllArgsConstructor
public class KeycloakCredentials {
    private String type;
    private String value;
    private boolean temporary;

}