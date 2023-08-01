package net.aroder.TripTracker.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * An entity class representing a User.
 */
@Entity(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

   @Id
   private String id;
   private String firstName;
   private String surname;
   private String email;
   @Column(columnDefinition = "boolean default false")
   private boolean tosAccepted;
   private List<String> roles = new ArrayList<>();
   @ManyToOne
   @JoinColumn(name="organizer_company_id")
   private OrganizerCompany organizerCompany;

   @ManyToOne
   @JoinColumn(name="dispatcher_company_id")
   private DispatcherCompany dispatcherCompany;

   @OneToMany(mappedBy = "driver")
   private List<Trip> trips;
   private String phoneNumber;

   public boolean isValid() {
      return this.email != null && validEmail() && this.firstName != null && this.surname != null;
   }

   private boolean validEmail(){
      String regexPattern = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
      Pattern emailPattern = Pattern.compile(regexPattern);
      return emailPattern.matcher(this.email).matches();
   }
}
