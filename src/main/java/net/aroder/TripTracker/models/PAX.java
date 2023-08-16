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

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("PAX(");
        builder.append("id=").append(this.id != null ? this.id : "").append(",");
        builder.append("status=").append(this.status != null ? this.status : null).append(",");
        builder.append("firstName=").append(this.firstName != null ? this.firstName : "").append(",");
        builder.append("surname=").append(this.surname != null ? this.surname : "").append(",");
        builder.append("flight=").append(this.flight != null ? this.flight : "").append(",");
        builder.append("pickUpTime=").append(this.pickUpTime != null ? this.pickUpTime.toString() : "").append(",");
        builder.append("ExpirationDate=").append(this.expirationDate != null ? this.expirationDate.toString() : "").append(",");
        builder.append("PickUpLocation=").append(this.pickUpLocation != null ? this.pickUpLocation : "").append(",");
        builder.append("Destination=").append(this.destination != null ? this.destination : "").append(",");
        builder.append("immigration=").append(this.immigration).append(",");
        builder.append("organization=").append(this.organization != null ? this.organization : "").append(",");
        builder.append("remarks=").append(this.remarks != null ? this.remarks : "").append(",");
        builder.append("paxValid=").append(this.paxValid).append(",");
        builder.append("poNumber=").append(this.poNumber != 0 ? this.poNumber : "").append(",");
        builder.append("harbour=").append(this.harbour != null ? this.harbour.getName() : "").append(",");
        builder.append("ship=").append(this.ship != null ? this.ship.getName() : "").append(",");
        builder.append("trip=").append(this.trip != null ? this.trip.getId() : "").append(",");
        builder.append("error=").append(this.error != null ? this.error : "").append(",");
        builder.append("cost=").append(this.cost != null ? this.cost : "");
        builder.append(")");

        return builder.toString();
    }
}
