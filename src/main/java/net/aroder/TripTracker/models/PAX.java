package net.aroder.TripTracker.models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

/**
 * An entity class representing a PAX (Passenger) entity.
 */
@Entity
@Data
public class PAX {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String status;
    private String firstName;
    private String surname;
    private String flight;
    private Timestamp pickUpTime;
    private Timestamp expirationDate;
    private String pickUpLocation;
    private String destination;
    private boolean immigration;
    private String organization;
    private String remarks;
    private Boolean paxValid;
    private Long poNumber;
    @ManyToOne
    private Location harbour;
    @ManyToOne
    private Ship ship;
    @ManyToOne
    private Trip trip;
    @ManyToOne
    private DomainFile adminReport;
    @ManyToOne
    private DomainFile dispatchReport;
    @ManyToOne
    private DomainFile organizerReport;
    private String error;
    private Double cost;

    /**
     * Checks if the PAX is valid.
     *
     * @return True if the PAX is valid, false otherwise.
     */
    @JsonIgnore
    public boolean isValid() {
        return firstName != null && surname != null && pickUpTime != null && pickUpLocation != null && destination != null && poNumber != null;
    }
}
