package net.aroder.TripTracker.models.DTOs.AuthDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A data class representing the body for a login request.
 */
@Data
@AllArgsConstructor
public class LoginRequest {
   private String email;
   private String password;
}
