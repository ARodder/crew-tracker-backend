package net.aroder.TripTracker.models.DTOs.UserDTOs;

import lombok.Data;

@Data
public class PasswordUpdateRequest {
    private String currentPassword;
    private String newPassword;
}
