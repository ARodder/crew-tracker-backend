package net.aroder.TripTracker.models;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An entity class representing a Trip.
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private User driver;
    private Timestamp pickUpTime;
    private Timestamp expirationDate;
    @OneToMany(mappedBy = "trip")
    private List<PAX> passengers = new ArrayList<>();
    @ManyToOne
    private Location pickUpLocation;
    @ManyToOne
    private Location destination;
    private Boolean immigration;
    private Boolean cancelFee;
    private Long poNumber;
    private Double subContractorPrice;
    private Double externalPrice;
    private String passengerRemarks;
    private String driverRemarks;
    @ManyToOne
    private Region region;
    @ManyToOne
    private Ship ship;
    private String status;
    @ManyToOne
    private OrganizerCompany organizerCompany;
    private Integer distance;
    private Boolean isElectrical;
    @ManyToOne
    private Location harbour;
    private Boolean adminReported;
    private Boolean dispatchReported;
    private Boolean organizerReported;

    /**
     * Adds a passenger to the trip.
     *
     * @param pax The passenger to add.
     */
    public void addPassenger(PAX pax) {
        this.passengers.add(pax);
    }

    /**
     * Adds multiple passengers to the trip.
     *
     * @param paxList The list of passengers to add.
     */
    public void addPassengers(List<PAX> paxList) {
        passengers.addAll(paxList);
    }

    /**
     * Removes a passenger from the trip.
     *
     * @param pax The passenger to remove.
     * @return The removed passenger.
     */
    public PAX removePassenger(PAX pax) {
        this.passengers.remove(pax);
        return pax;
    }

    /**
     * Checks if all passengers have canceled.
     *
     * @return true if all passengers have canceled, false otherwise.
     */
    public boolean allPassengersCanceled() {
        return this.passengers.stream().filter(passenger -> !passenger.getStatus().equals("cancel")).toList().size() == 0;
    }

    /**
     * Adds a driver remark.
     *
     * @param remark The driver remark to add.
     */
    public void addDriverRemark(String remark) {
        if(this.driverRemarks == null){
            driverRemarks = remark;
        }else {
            this.driverRemarks += "\n" + remark;
        }
    }


    public Integer getActivePassengers() {
        return this.passengers.stream().filter(passenger -> !passenger.getStatus().equals("cancel")).toList().size();
    }

    /**
     * Adds a passenger remark.
     *
     * @param remark The passenger remark to add.
     */
    public void addPassengerRemark(String remark) {

        if(this.passengerRemarks == null){
            passengerRemarks = remark;
        }else {
            this.passengerRemarks += ";" + remark;
        }
    }

    public Trip clone() {
        return new Trip(null,
                null,
                this.pickUpTime,
                this.expirationDate,
                new ArrayList<>(),
                this.pickUpLocation,
                this.destination,
                null,
                false,
                this.poNumber,
                null,
                null,
                "",
                "",
                this.region,
                this.ship,
                "Created",
                this.organizerCompany,
                null,
                null,
                this.harbour,
                false,
                false,
                false);
    }
}
