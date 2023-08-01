package net.aroder.TripTracker.models;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

/**
 * An entity class representing an Organizer Company.
 */
@Entity
@Data
public class OrganizerCompany {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;
   private String name;
   @OneToMany(mappedBy="organizerCompany")
   private List<User> employees;
   @OneToMany(mappedBy="organizerCompany")
   private List<Ship> ships;

}
