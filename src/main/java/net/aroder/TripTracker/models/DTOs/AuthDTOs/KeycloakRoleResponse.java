package net.aroder.TripTracker.models.DTOs.AuthDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KeycloakRoleResponse {
    private String id;
    private String name;
}

