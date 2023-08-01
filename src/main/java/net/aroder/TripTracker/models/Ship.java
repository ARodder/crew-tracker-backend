package net.aroder.TripTracker.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An entity class representing a Ship.
 */
@Entity
@Data
@NoArgsConstructor
public class Ship {
   
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;
   @Column(unique = true)
   private String name;
   private Long imo;
   @ManyToOne
   private OrganizerCompany organizerCompany;

   /**
    * Constructs a ship with the given name.
    *
    * @param name The name of the ship.
    */
   public Ship(String name){
      this.name = name;
   }
}
