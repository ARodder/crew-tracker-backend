package net.aroder.TripTracker.models.DTOs.AuthDTOs;

import lombok.Data;


/**
 * A data class representing the response body for a login attempt.
 */
@Data
public class LoginResponse {
   private String access_token;
   private String refresh_token;
   private String expires_in;
   private String refresh_expires_in;
   private String token_type;
}
