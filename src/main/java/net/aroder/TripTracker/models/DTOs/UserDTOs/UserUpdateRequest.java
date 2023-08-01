package net.aroder.TripTracker.models.DTOs.UserDTOs;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String firstName;
    private String surname;
    private String email;
    private String phoneNumber;
}
