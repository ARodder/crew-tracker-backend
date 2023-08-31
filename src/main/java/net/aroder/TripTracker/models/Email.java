package net.aroder.TripTracker.models;

import lombok.Data;

import java.util.Objects;
import java.util.regex.Pattern;

@Data
public class Email {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    private final String recipient;
    private final Subject emailSubject;
    private final String emailBody;


    public enum Subject {
        INITIAL_PASSWORD("Initial password - CrewTracker");

        private final String text;

        Subject(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    /**
     * Constructs an email based on the subject chosen while creating the email
     * object, also checks if the email recipient is valid at the start
     *
     * @param recipient    email address of the recipient
     * @param emailSubject an enum of predefined values
     * @throws IllegalArgumentException if email does not match regex or when subject is invalid
     */
    public Email(String recipient, Subject emailSubject, String content) {
        if (!isValidEmailAddress(recipient.toLowerCase())) {
            throw new IllegalArgumentException("Invalid email address: " + recipient);
        }

        this.recipient = recipient.toLowerCase();
        this.emailSubject = emailSubject;

        if (Objects.requireNonNull(emailSubject) == Subject.INITIAL_PASSWORD) {
            emailBody = "<center><h1 style='color:#FED400; font-size:4rem;'>CrewTracker Norge</h1><h2>Initial Password</h2><p style='color:#ffffff; margin-bottom:2rem; margin-top:-0.05rem;'>We advice you to change the password upon your first login</p><div style='width:95%; height:5rem; background-color:unset; border-color:#FED400; border-style:dotted; border-width:0.2rem;  border-radius:1rem; padding:0.5rem; color:unset; font-size:1.5rem;'><h1>" + content + "</h1></div></center>";
        } else {
            throw new IllegalArgumentException("Invalid email subject: " + emailSubject.getText());
        }
    }

    /**
     * Checks if a string in a valid email
     *
     * @param emailAddress the recipient's email address
     * @return true/false
     */
    private boolean isValidEmailAddress(String emailAddress) {
        return EMAIL_PATTERN.matcher(emailAddress).matches();
    }

}
