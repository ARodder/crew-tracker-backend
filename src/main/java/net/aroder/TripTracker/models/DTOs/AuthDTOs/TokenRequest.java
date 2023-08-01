package net.aroder.TripTracker.models.DTOs.AuthDTOs;

import lombok.Data;

/**
 * An object containing a token received from a request.
 */
@Data
public class TokenRequest {
   private String token;
}
