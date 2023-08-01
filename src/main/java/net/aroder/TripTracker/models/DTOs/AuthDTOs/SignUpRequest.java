package net.aroder.TripTracker.models.DTOs.AuthDTOs;

import java.util.regex.Pattern;

import lombok.Data;

/**
 * A class to manage Sign up request data.
 */
@Data
public class SignUpRequest {
    private String email;
    private String firstName;
    private String surname;
    private String phoneNumber;

    /**
     * Checks if the SignUpRequest data is valid.
     * @return boolean based on if the signup data is valid or not.
     */
    public boolean isValid(){
        return this.email != null && validEmail() && this.firstName != null && this.surname != null;
    }

    /**
     * Validates the email of the SignUpRequest.
     * @return boolean based on if the email is valid or not.
     */
    private boolean validEmail(){
        String regexPattern = "^[ÆØÅæøåa-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[ÆØÅæøåa-zA-Z0-9.-]+$";
        Pattern emailPattern = Pattern.compile(regexPattern);
        return emailPattern.matcher(this.email).matches();
    }
}